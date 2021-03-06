package xyz.disconnect24.bot;

/*
 * The MIT License
 *
 * Copyright 2017 Disconnect24 and its contributors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import xyz.disconnect24.bot.commands.botadm.Bash;
import xyz.disconnect24.bot.commands.botadm.Eval;
import xyz.disconnect24.bot.commands.botadm.Shutdown;
import xyz.disconnect24.bot.commands.tools.*;
import xyz.disconnect24.bot.commands.wii.*;
import xyz.disconnect24.bot.events.ServerLog;
import xyz.disconnect24.bot.loader.Config;

import javax.security.auth.login.LoginException;

/**
 * Add all commands, and start all events.
 *
 * @author Spotlight and Artu
 */

public class Main extends ListenerAdapter {

    private static Config config;
    private static JedisPool pool;
    private static String prefix;
    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws LoginException, IllegalArgumentException, RateLimitedException, InterruptedException {
        try {
            config = new Config();
        } catch (Exception e) {
            logger.error(e.toString());
            return;
        }

        // Register commands
        EventWaiter waiter = new EventWaiter();

        CommandClientBuilder client = new CommandClientBuilder();
        client.setEmojis(Const.DONE_E, Const.WARN_E, Const.FAIL_E);
        client.setOwnerId("" + config.getPrimaryOwner());

        // Convert Long[] of secondary owners to String[] so we can set later
        Long[] owners = config.getSecondaryOwners();
        String[] ownersString = new String[owners.length];

        for (int i = 0; i < owners.length; i++) {
            ownersString[i] = String.valueOf(owners[i]);
        }

        // Set all co-owners
        client.setCoOwnerIds(ownersString);
        prefix = config.getPrefix();
        client.setPrefix(prefix);

        // Create JedisPool for usage elsewhere
        pool = new JedisPool(new JedisPoolConfig(), "localhost");
        client.addCommands(
                // Bot administration
                new Bash(),
                new Eval(pool, config),
                new Shutdown(),

                // Tools
                new BotConfig(),
                new UserInfo(),
                new Invite(),
                new MailParseCommand(),
                new Ping(),

                // Wii-related
                new Codes(pool),
                new Add(pool),
                new ErrorInfo(config.isDebug()),
                new DNS(),
                new Wads()
        );

        //JDA Connection
        new JDABuilder(AccountType.BOT)
                .setToken(config.getToken())
                .setStatus(config.getStatus())
                .setGame(Game.playing(Const.GAME_0))
                .addEventListener(waiter)
                .addEventListener(client.build())
                .addEventListener(new Main())
                .addEventListener(new ServerLog())
                .buildBlocking();
    }

    @Override
    public void onReady(ReadyEvent event) {
        logger.info("Done loading!");
        // Check if we need to set a game
        if (config.getPlaying().isEmpty()) {
            event.getJDA().getPresence().setGame(Game.playing("Type " + prefix + "help"));
        } else {
            event.getJDA().getPresence().setGame(Game.playing(config.getPlaying()));
        }
    }

    @Override
    public void onShutdown(ShutdownEvent event) {
        pool.destroy();
    }
}
