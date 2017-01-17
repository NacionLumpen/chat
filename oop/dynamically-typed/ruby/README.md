[back to main readme](/README.md)


## ruby implementation using POO

This is just 4 fun solution, don't use in real software xP

## folder structure

like a gem folder structure, you know...

```
.
└── ruby
    └─  Gemfile
    └─  changelog.md
    └─  lib
      └── nlchat
        └── nl_server_basic.rb
    └─  Rakefile
    └─  README.md
    └─  test
      └── nl_chat_command_parser_test.rb

```

## how to use

From now using rake. set your directory inside of ruby folder


### launching the server

    $ cd chat/opp/dinamically-typed/ruby
    $ rake server:run

### using telnet like client

    $ telnet 127.0.0.1 1212

### run test

    $ rake test

    only test unit for classes

[back to main readme](/README.md)
