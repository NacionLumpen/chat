class NLChatNotifications

  def self.msg(nick,message)
    return "MSG #{nick}: #{message}\n"
  end

  def self.joined(nick)
    return "JOINED #{nick}\n"
  end

  def self.rename(old_nick,new_nick)
    return "RENAME #{old_nick} #{new_nick}\n"
  end

  def self.left(nick,parting_message)
    return "LEFT #{nick}#{parting_message.nil? ? '' : ':';} #{parting_message}\n"
  end

  def self.kicked(kicked,by)
    return "#{by} KICKED TO #{kicked}\n"
  end

  def self.unknow_command
    return "UNKNOWN COMMAND\n"
  end

end
