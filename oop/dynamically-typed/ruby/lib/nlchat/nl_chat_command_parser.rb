class NLChatCommandParser

  # def unknow_command?(command)
  #   #return true if command =~ /^[A-Z]+\s[\w\W]*$/ and !command_nick?(command) and !command_msg?(command) and
  # end

  @@max_bytes = 255

  def self.max_bytes
    @@max_bytes
  end

  def self.empty?(command)
    command.to_s.gsub(/[^[:print:]]/,'').empty?
  end

  def self.exceed_length?(command)
    command.to_s.bytesize > @@max_bytes
  end

  def self.command_nick?(command)
    # return true if command =~ /^NICK\s[a-zA-Z\d\-_]{1,10}$/
    return true if command =~ /^NICK\s[\w\W]{1,}$/
  end

  def self.nick(command)
    # NICK <nickname>
    result=command[5..-1]||""unless command.nil?
    result
  end

  def self.wellformed_nick?(nick)
    return true if nick =~ /^[a-zA-Z\d\-_]{1,10}$/
  end

  def self.command_msg?(command)
    return true if command =~ /^MSG\s[\w\W]*$/
  end

  def self.command_names?(command)
    return true if command =~ /^NAMES\s{0,1}$/
  end

  def self.command_quit?(command)
    return true if command =~ /^QUIT[\w\W]*$/
  end

  def self.quit_message(command)
    return command[5..-1]
  end

  def self.command_kick?(command)
    return true if command =~ /^KICK\s[a-zA-Z\d\-_]{1,10}$/
  end

  def self.who_kick(command)
    #return /[a-zA-Z\d\-_]*$/.match(command)[0] if command =~ /^KICK\s[a-zA-Z\d\-_]{1,10}$/
    return command[5..-1]
  end

  def self.command_kick_nick(command)
    #like nick to kick
  end

  def self.valid?(command)
  end


private
  def self.remove_characters_non_printables(command)
    return command.gsub!(/[^[:print:]]/,'')
  end

end
