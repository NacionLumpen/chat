require 'socket'


class NLChatServerBasic

  def initialize(port=1212)
    @clients = Hash.new # { :nick => TCPSocket}
    @running_status = false
    @socket_service = TCPServer.new(port)
    puts "NLChatServer listening at #{port}"

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
      events_to_resolve[0].each do |event|
        if event==@socket_service
          # socket_service must accept new connection
          newclient = @socket_service.accept
          newclient.puts "Welcome to NacionLumpen, please enter your nick" # TODO hardcode string
          nickname = newclient.gets.strip!
          # check for nicknames already in use
          while @clients.has_key? nickname.to_sym
              newclient.puts "Nickname already in use. Enter new nickname: "
              nickname = newclient.gets.strip!
          end
          newclient.puts "nick accepted, enjoy."
          @clients[nickname.to_sym] = newclient
        else
          # some socket of client has write something, broadcast_message
          nickname = @clients.key(event)
          message = event.gets
          @clients.values.each do |socketClient|
            #puts "#{nickname}:#{message}"
            # socketClient.puts "#{nickname}:#{message}" unless socketClient==event
            socketClient.write "#{nickname}:#{message}" unless socketClient==event
          end
        end
      end
    end
  end

end
