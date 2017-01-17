require 'socket'
require_relative 'nl_chat_command_parser'
require_relative 'nl_chat_notification'
require_relative 'nl_chat_response'


class NLChatServerBasic

  def initialize(port=1212,msg_welcome=nil,msg_nick_in_use=nil,msg_nick_accepted=nil)
    @msg_welcome = msg_welcome || "Welcome to NacionLumpen, change your nick with \"NICK <nickname>\""
    @msg_nick_in_use = msg_nick_in_use || "Nickname already in use. Enter new nickname: "
    @msg_nick_accepted = msg_nick_accepted || "nick accepted, enjoy."

    @clients = Hash.new # { :nick => TCPSocket}
    @running_status = false
    @socket_service = TCPServer.new(port)
    puts "NLChatServer listening at #{port}"
  end

  def notify(message,exclude=nil)
    @clients.values.each do |socketClient|
      socketClient.write message unless socketClient==exclude
    end
  end

  def split_names_in_messages_without_exceed(names,max_bytes=NLChatResponse.max_bytes)
    result = []
    i = 0
    names.each do |name|
      result[i]="" if result[i].nil?
      i=i+1 if NLChatResponse.names_prefix_bytes+result[i].length+name.length > NLChatResponse.max_bytes
      result[i] << " #{name}"
    end
    result[i+1]= NLChatResponse.names
    # 202 <nick>* Complete or rest of the list of nicks
    # 203 <nick>* Part of the list of nicks. It will be followed by another response with code 200 or 201
    result
  end

  def nick_already_in_use?(nick)
    !@clients[nick.to_sym].nil?
  end

  def generate_nickname
    i=0
    while nick_already_in_use?("anon#{i}") do
      i=i+1
    end
    "anon#{i}"
  end



  def service
    @running_status = true
    while @running_status

      # wait for events loop,
      # check https://ruby-doc.org/core-2.3.1/IO.html#method-c-select
      # select(read_array [, write_array [, error_array [, timeout]]]) → array or nil
      #   read_array  -  an array of IO objects that wait until ready for read
      #   write_array -  an array of IO objects that wait until ready for write
      #   error_array -  an array of IO objects that wait for exceptions
      #   timeout     -  a numeric value in second

      events_to_resolve = IO::select([@socket_service].concat(@clients.values),nil,nil,nil)
      events_to_resolve[0].each do |socket_trigger_event|
        if socket_trigger_event==@socket_service
          # socket_service must accept new connection
          new_client_socket = @socket_service.accept
          new_client_socket.puts @msg_welcome
          generated_nickname = generate_nickname
          @clients[generated_nickname.to_sym] = new_client_socket
          notify(NLChatNotifications.joined(generated_nickname),new_client_socket)
        else
          received_nickname = @clients.key(socket_trigger_event)
          received_message = socket_trigger_event.gets.strip!

          if NLChatCommandParser.command_msg?(received_message)
            socket_trigger_event.write(NLChatResponse.success)
            notify(NLChatNotifications.msg(received_nickname,received_message[4..-1]),socket_trigger_event)
          elsif NLChatCommandParser.command_quit?(received_message)
            socket_trigger_event.write(NLChatResponse.success)
            parting_message = NLChatCommandParser.quit_message(received_message)
            socket_trigger_event.close
            @clients.delete(received_nickname)
            notify(NLChatNotifications.left(received_nickname,parting_message))
          elsif NLChatCommandParser.command_nick?(received_message)
            #if NLChatCommandParser.wellformed_nick?(received_message[5,-1]) # TODO: dependence this line knows behavior
            new_nick = NLChatCommandParser.nick(received_message)
            valid_nick = NLChatCommandParser.wellformed_nick?(new_nick)
            socket_trigger_event.write(NLChatResponse.error_malformed_nick) unless valid_nick
            socket_trigger_event.write(NLChatResponse.error_nick_in_use) if valid_nick and !@clients[new_nick.to_sym].nil?
            if valid_nick and @clients[new_nick.to_sym].nil?
              socket_trigger_event.write(NLChatResponse.success)
              @clients[new_nick.to_sym] = @clients[received_nickname]
              @clients.delete(received_nickname)
              notify(NLChatNotifications.rename(received_nickname,new_nick),socket_trigger_event)
            end
          elsif NLChatCommandParser.command_names?(received_message)
            # TODO: be careful if message can more than 255? split?
            # TODO: ask whitespace as separator between clients?
            #socket_trigger_event.write(@clients.keys)
            messages = split_names_in_messages_without_exceed(@clients.keys)
            messages.each do |msg|
              socket_trigger_event.write(msg)
            end
          elsif NLChatCommandParser.command_kick?(received_message)
            # we want to democratize fun! xP
            will_kick = NLChatCommandParser.who_kick(received_message)
            socket_for_kick = @clients[will_kick.to_sym]
            unless socket_for_kick.nil?
              socket_for_kick.write(NLChatResponse.you_was_kicked_by(received_nickname))
              socket_for_kick.close
              @clients.delete(will_kick.to_sym)
              socket_trigger_event.write(NLChatResponse.success)
              # TODO: ask below
              notify(NLChatNotifications.kicked(will_kick,received_nickname),socket_trigger_event)
            else
              socket_trigger_event.write(NLChatResponse.nick_to_kick_doesnt_exist)
            end
          else
            socket_trigger_event.write(NLChatResponse.error_unknown_command)
          end
        end
      end
    end
  end

end
