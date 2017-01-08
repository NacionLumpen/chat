class NLChatResponse

  @@max_bytes = 255

  def self.max_bytes
    @@max_bytes
  end

  def self.success(msg=nil)
    "200 OK\n"
  end

  def self.names_prefix_bytes
    "203 ".length
  end

  def self.names_exceed_max_bytes?(names)
    result = false
    result = true if !names.nil? and NLChatResponse.names_prefix_bytes + names.lenth > @@max_bytes
    return result
  end

  def self.names(names=nil,complete=true)
    if names.nil?
      result ="\n200 OK\n"
    elsif complete
      result = "202 #{names}"
    elsif
      result = "203 #{names}"
    end
    result
  end

  def self.error_malformed_nick()
    "301\n"
  end

  def self.error_nick_in_use()
    "302\n"
  end

  def self.nick_to_kick_doesnt_exist
    "303\n"
  end

  def self.error_unknown_command()
    "421\n"
  end

  def self.you_was_kicked_by(name)
    "you was kicked by #{name}, you should search revenge.\n"
  end
end
