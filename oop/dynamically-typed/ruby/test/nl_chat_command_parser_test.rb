require_relative 'test_helper'
# require 'test_helper'



class TestNLChatCommandParser < MiniTest::Unit::TestCase
  def setup
  end

  def test_that_it_has_a_version_number
    refute_nil ::NLChat::VERSION
  end

  def test_empty
    assert NLChatCommandParser.empty?("\r\n")
    assert NLChatCommandParser.empty?("")
    refute NLChatCommandParser.empty?("*")
    refute NLChatCommandParser.empty?("NIKC anon101")
  end

  def test_length
    refute NLChatCommandParser.exceed_length?("NICK anon101")
    refute NLChatCommandParser.exceed_length?("MSG")
    refute NLChatCommandParser.exceed_length?("NAMES")
    refute NLChatCommandParser.exceed_length?("QUIT bye")
    refute NLChatCommandParser.exceed_length?("KICK *")
    refute NLChatCommandParser.exceed_length?("***************************************************************************************************************************************************************************************************************************************************************")
    assert NLChatCommandParser.exceed_length?("****************************************************************************************************************************************************************************************************************************************************************")
  end

  def test_command_nick_right
    assert NLChatCommandParser.command_nick?("NICK a")
    assert NLChatCommandParser.command_nick?("NICK abcd")
    assert NLChatCommandParser.command_nick?("NICK anonimous1")
    assert NLChatCommandParser.command_nick?("NICK -")
    assert NLChatCommandParser.command_nick?("NICK _")
    assert NLChatCommandParser.command_nick?("NICK _a_")
    assert NLChatCommandParser.command_nick?("NICK _-a-_")
    # below code is becouse command is right but nickname is Malformed
    assert NLChatCommandParser.command_nick?("NICK anonimous01 asfdf fsadf")
    assert NLChatCommandParser.command_nick?("NICK %")
    assert NLChatCommandParser.command_nick?("NICK \"")
    assert NLChatCommandParser.command_nick?("NICK '")
    assert NLChatCommandParser.command_nick?("NICK $")
    assert NLChatCommandParser.command_nick?("NICK *")
    assert NLChatCommandParser.command_nick?("NICK +")
  end

  def test_command_nick_wrong
    refute NLChatCommandParser.command_nick?("NICK ")
  end

  def test_nick
    assert_nil NLChatCommandParser.nick(nil)
    assert_equal "", NLChatCommandParser.nick("NICK ")
    assert_equal "a", NLChatCommandParser.nick("NICK a")
    assert_equal "aaaaaaaaaaaaaaaa", NLChatCommandParser.nick("NICK aaaaaaaaaaaaaaaa")
  end

  def test_wellformed_nick
    assert NLChatCommandParser.wellformed_nick?("a")
    assert NLChatCommandParser.wellformed_nick?("anonimous1")
    assert NLChatCommandParser.wellformed_nick?("-")
    assert NLChatCommandParser.wellformed_nick?("_")
    assert NLChatCommandParser.wellformed_nick?("_a_")
    assert NLChatCommandParser.wellformed_nick?("_-a-_")

    refute NLChatCommandParser.wellformed_nick?("anonimous01 asfdf fsadf")
    refute NLChatCommandParser.wellformed_nick?("%")
    refute NLChatCommandParser.wellformed_nick?("\"")
    refute NLChatCommandParser.wellformed_nick?("'")
    refute NLChatCommandParser.wellformed_nick?("$")
    refute NLChatCommandParser.wellformed_nick?("*")
    refute NLChatCommandParser.wellformed_nick?("+")
  end

  def test_command_msg_right
    assert NLChatCommandParser.command_msg?("MSG \n")
    assert NLChatCommandParser.command_msg?("MSG \r\n")
    assert NLChatCommandParser.command_msg?("MSG hello world")
  end

  def test_command_msg_wrong
    refute NLChatCommandParser.command_msg?("MSG")
  end

  def test_command_names_right
    assert NLChatCommandParser.command_names?("NAMES \n")
  end

  def test_command_names_wrong
  end

  def test_command_quit_right
    assert NLChatCommandParser.command_quit?("QUIT")
    assert NLChatCommandParser.command_quit?("QUIT \n")
    assert NLChatCommandParser.command_quit?("QUIT bye to all")
    assert NLChatCommandParser.command_quit?("QUIT bye to all\n")
  end

  def test_command_quit_wrong
  end

  def test_who_kick
    assert_equal "anon1", NLChatCommandParser.who_kick("KICK anon1")
    assert_equal "a-1", NLChatCommandParser.who_kick("KICK a-1")
    assert_equal "a_non1", NLChatCommandParser.who_kick("KICK a_non1")
    assert_equal "-anon1-", NLChatCommandParser.who_kick("KICK -anon1-")
    assert_equal "-ANON1-", NLChatCommandParser.who_kick("KICK -ANON1-")
  end

  def test_command_kick_right
    assert NLChatCommandParser.command_kick?("KICK a")
    assert NLChatCommandParser.command_kick?("KICK anonimous1")
  end

  def test_command_kick_wrong
    refute NLChatCommandParser.command_kick?("KICK")
    refute NLChatCommandParser.command_kick?("KICK ")
    refute NLChatCommandParser.command_kick?("KICK \n")
    refute NLChatCommandParser.command_kick?("KICK anonimous01")
  end

  def test_split_long_notifications_names

  end

  def test_unknow_command
  end

  # def test_that_will_be_skipped
  #   skip "test this later"
  # end

end
