package com.project.SpringPetBot.service;

import com.project.SpringPetBot.config.BotConfig;
import com.project.SpringPetBot.model.User;
import com.project.SpringPetBot.model.UserRepository;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;
    final BotConfig config;

    static final String HELP_TEXT = "Этот бот является демо - версией бота, который создан для понимания о ботах в целом.  \n";

    public TelegramBot(BotConfig config) {

        this.config = config;
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand("/start","Приветствие"));
        listofCommands.add(new BotCommand("/help","Справка"));
        try{
            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(),null));
        }
        catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }

    }

    @Override
    public String getBotUsername() {

        return config.getBotName();
    }

    @Override
    public String getBotToken() {

        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if(update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch(messageText) {
                case "/start":

                        registerUser(update.getMessage());
                        startCommandReceived(chatId,update.getMessage().getChat().getFirstName());
                        break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;

                default:

                        sendMessage(chatId, " Данная команда не поддерживается");


            }
        }

    }

    private void registerUser(Message msg) {
        if(userRepository.findById(msg.getChatId()).isEmpty()) {
            var chatId = msg.getChatId();
            var chat = msg.getChat();

            User user = new User();
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
            userRepository.save(user);
            log.info("user saved:" + user);
        }
    }

    private void startCommandReceived(long chatId, String name)  {
        String answer = EmojiParser.parseToUnicode("Привет, " + name + "!" + ":blush:");
        //String answer = "Привет, " + name + "!";
        log.info("Replied to user" + name);
        sendMessage(chatId, answer);

    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        try{
            execute(message);

        }
        catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());

        }
    }

}
