module SerieBot
    module Commands
        extend Discordrb::Commands::CommandContainer
        extend Discordrb::EventContainer
        require 'open-uri'

        # Migrated from Yuu-Chan's Yuu module
        command (:wads) do |event|
            event.channel.start_typing
            wads = "**__ RiiConnect24 WADs: __**\n"
            wads << "Latest IOS31: http://pokeacer.xyz/owncloud/index.php/s/qMm01pal7hN2wDU/download\n"
            wads << "Latest IOS35: http://pokeacer.xyz/owncloud/index.php/s/S7uFituZzlt49oY/download\n"
            wads << "Latest IOS80: http://pokeacer.xyz/owncloud/index.php/s/m1K8KW8Tsbn4zTS/download\n"
            wads << 'Latest IOS251: http://pokeacer.xyz/owncloud/index.php/s/QxCideE7BGy2l5f/download'
            begin
                event.user.pm(wads)
            rescue Discordrb::Errors::NoPermission
                event.respond("❌ Sorry, but it looks like you're blocking DMs.")
                break
            end
            event.respond("👌")
        end

        command(:error, max_args: 1, min_args: 1) do |event, code|
            # Start typing so the user knows something's going on
            event.channel.start_typing
            # Validate
            if code.to_i.to_s == code
                # 0 returns an empty array (see http://forum.wii-homebrew.com/index.php/Thread/57051-Wiimmfi-Error-API-has-an-error/?postID=680936#post680936)
                # We'll just treat it as an error.
                if code == '0'
                    event.respond("❌ Enter a valid error code!")
                    break
              end
                # Determine method
                # Per http://forum.wii-homebrew.com/index.php/Thread/57051-Wiimmfi-Error-API-has-an-error/?postID=680943#post680943
                # it was recommended to use "t=<code>" for dev and "e=<code>" for prod due to statistical reasons.
                method = if Config.debug
                             "t=#{code}"
                         else
                             "e=#{code}"
                         end
                # Grab JSON
                json_string = open("https://wiimmfi.de/error?#{method}&m=json").read
                array = JSON.parse(json_string, symbolize_names: true)
                # This is a hash wrapped in an array, so go grab it.
                if array[0][:found] == 1
                    data = array[0]
                    messageToSend = ''
                    # Infolist will have all the table things
                    data[:infolist].each do |row|
                        info = row[:info]

                        otherLinkInfo = info

                        # Cycle through all matches
                        loop do
                            # Links
                            linkMatches = /<a href\s*=\s*"http([^"]*)">([^"]*)<\/a>/.match(otherLinkInfo)
                            break if linkMatches.nil?
                            # Replaces matches with [title](http<url>) (Discord embed thing)
                            # We start the URL with http because of the regex.
                            otherLink = "[#{linkMatches[2]}](http#{linkMatches[1]})"
                            otherLinkInfo = otherLinkInfo.gsub(linkMatches[0], otherLink)
                        end
                        # For formatting.
                        oneBold = otherLinkInfo.gsub('<b>', '**')
                        twoBold = oneBold.gsub('</b>', '**')
                        oneItalic = twoBold.gsub('<i>', '*')
                        twoItalic = oneItalic.gsub('</i>', '*')

                        messageToSend += "#{row[:type]} for error #{row[:name]}: #{twoItalic}\n"
                    end

                    event.channel.send_embed do |e|
                        e.title = "Here's information about your error:"
                        e.description = messageToSend.to_s
                        e.colour = "#D32F2F"
                        e.footer = Discordrb::Webhooks::EmbedFooter.new(text: 'All information is from Wiimmfi.')
                    end
                    # This break is super important, otherwise it messages all of data[:infolist]
                    # Why? I don't know, just please don't remove this
                    break
                else
                    event.respond("❌ Could not find the specified error from Wiimmfi.")
                    break
                end
            else
                event.respond("❌ Enter a valid error code!")
                break
          end
        end
        command(:gametdb, max_args: 2, min_args: 2) do |event, platform, code|
            platforms = %w(Wii WiiU PS3 3DS DS)
            if platform == '' || platform.nil?
                event.respond("❌ Enter a valid platform!")
                break
            end
            unless platforms.include?(platform)
                event.respond("❌ Enter a valid platform!")
                break
            end
            if code == '' || code.nil?
                event.respond("❌ Enter a valid game code!")
                break
            end
            event.respond("http://gametdb.com/#{platform}/#{code}")
        end

        command(:instructions, max_args: 0, min_args: 0) do |event|
            event.respond('https://riiconnect24.net/instructions/')
        end

        command(:dns) do |event|
            event.respond("`185.82.21.64` should be your primary DNS.
            `8.8.8.8` (Google's DNS) can be your secondary DNS server.")
        end

        command(:seriously) do |event|
          "I'm serious, I actually did"
        end
    end
end
