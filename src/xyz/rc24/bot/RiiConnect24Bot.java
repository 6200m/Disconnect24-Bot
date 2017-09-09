/*
 * The MIT License
 *
 * Copyright 2017 Artu.
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

package xyz.rc24.bot;

import com.jagrosh.jdautilities.commandclient.CommandClientBuilder;
import com.jagrosh.jdautilities.waiter.EventWaiter;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.SimpleLog;
import xyz.rc24.bot.commands.botadm.Shutdown;
import xyz.rc24.bot.commands.tools.Codes;
import xyz.rc24.bot.commands.tools.UserInfo;
import xyz.rc24.bot.loader.Config;
import xyz.rc24.bot.utils.CodeManager;

import javax.security.auth.login.LoginException;
import java.io.*;

/**
 * @author Artu
 */

public class RiiConnect24Bot extends ListenerAdapter {

    private static Config config;

    public static void main(String[] args) throws IOException, LoginException, IllegalArgumentException, RateLimitedException, InterruptedException {
        try {
            config = new Config();
        } catch (Exception e) {
            SimpleLog.getLog("Config").fatal(e);
            return;
        }

        CodeManager manager = new CodeManager();

        // Register commands and some other things
        EventWaiter waiter = new EventWaiter();

        CommandClientBuilder client = new CommandClientBuilder();
        client.useDefaultGame();
        client.setOwnerId(config.getPrimaryOwner());
        client.setCoOwnerIds(config.getSecondaryOwners());
        client.setEmojis(Const.DONE_E, Const.WARN_E, Const.FAIL_E);
        client.setPrefix(config.getPrefix());
        client.addCommands(
                new Codes(manager),
                new Shutdown(manager),
                new UserInfo()
        );

        //JDA Connection
        new JDABuilder(AccountType.BOT)
                .setToken(config.getToken())
                .setStatus(config.getStatus())
                .setGame(Game.of(Const.GAME_0))
                .addEventListener(waiter)
                .addEventListener(client.build())
                //.addEventListener(new Bot())
                .addEventListener(new RiiConnect24Bot())
                //.addEventListener(new Logging())
                .buildBlocking();
    }

    @Override
    public void onReady(ReadyEvent event) {
        SimpleLog.getLog("Bot").info("Done loading!");
        event.getJDA().getPresence().setGame(Game.of(config.getPlaying()));
    }
}