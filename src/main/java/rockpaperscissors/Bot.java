package rockpaperscissors;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.InlineQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.InlineQueryResultArticle;
import com.pengrad.telegrambot.request.AnswerInlineQuery;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bot {
    private final TelegramBot bot = new TelegramBot(System.getenv("BOT_TOKEN"));
    private final String PROCESSING_LABEL = "...";
    private final static List<String>opponentWins = new ArrayList<String>(){{
        add("20");
        add("01");
        add("12");
    }};
    private final static Map<String, String> items = new HashMap<String, String>(){{
        put("0", "✊");
        put("1", "✋");
        put("2", "✌");
    }};

    public void serve() {
        bot.setUpdatesListener(updates -> {
            updates.forEach(this::process);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void process(Update update) {
        Message message = update.message();
        CallbackQuery callbackQuery = update.callbackQuery();
        InlineQuery inlineQuery = update.inlineQuery();

        BaseRequest request = null;

        if(message != null && message.viaBot() != null && message.viaBot().username().equals("RockPaperScissors99bot")){
            InlineKeyboardMarkup replyMarkup = message.replyMarkup();
            if(replyMarkup == null){
                return;
            }

            InlineKeyboardButton[][]  buttons = replyMarkup.inlineKeyboard();

            if(buttons == null){
                return;
            }
            InlineKeyboardButton button = buttons[0][0];
            String buttonLabel = button.text();

            if(!buttonLabel.equals(PROCESSING_LABEL)){
                return;
            }

            Long chatId = message.chat().id();
            String senderName = message.from().firstName();
            String senderChose = button.callbackData();
            Integer messageId = message.messageId();

            request = new EditMessageText(chatId, messageId, message.text()).replyMarkup(
                    new InlineKeyboardMarkup(
                            new InlineKeyboardButton("✊")
                                    .callbackData(String.format("%d %s %s %s %d", chatId, senderName, senderChose, "0", messageId)),
                            new InlineKeyboardButton("✋")
                                    .callbackData(String.format("%d %s %s %s %d", chatId, senderName, senderChose, "1", messageId)),
                            new InlineKeyboardButton("✌")
                                    .callbackData(String.format("%d %s %s %s %d", chatId, senderName, senderChose, "2", messageId))
                    )
            );
        } else if(inlineQuery != null){
            InlineQueryResultArticle rock = buildInlineButton("rock", "✊Rock", "0");
            InlineQueryResultArticle paper = buildInlineButton("paper", "✋Paper", "1");
            InlineQueryResultArticle scissors = buildInlineButton("scissors", "✌Scissors", "2");

            request = new AnswerInlineQuery(inlineQuery.id(), rock, paper, scissors).cacheTime(1);
        }else if(callbackQuery != null){
            String [] data = callbackQuery.data().split(" ");
            if(data.length<4){
                return;
            }
            Long chatId = Long.parseLong(data[0]);
            String senderName = data[1];
            String senderChose = data[2];
            String opponentChose = data[3];
            int messageId = Integer.parseInt(data[4]);
            String opponentName = callbackQuery.from().firstName();

            if(senderChose.equals(opponentChose)){
                request = new EditMessageText(chatId, messageId,
                        String.format(
                                "%s and %s chose %s. Nobody wins",
                                senderName,
                                opponentName,
                                items.get(senderChose)
                        )
                );
            }else if(opponentWins.contains(senderChose+opponentChose)){
                request = new EditMessageText(
                        chatId, messageId,
                        String.format(
                                "%s chose %s and lost to %s, who chose %s",
                                senderName, items.get(senderChose),
                                opponentName, items.get(opponentChose)
                        )
                );
            } else{
                request = new EditMessageText(
                        chatId, messageId,
                        String.format(
                                "%s chose %s and lost to %s, who chose %s",
                                opponentName, items.get(opponentChose),
                                senderName, items.get(senderChose)
                        )
                );
            }
        }

        if(request != null) {
            bot.execute(request);
        }
    }

    private InlineQueryResultArticle buildInlineButton(String id, String title, String callbackData) {
        return new InlineQueryResultArticle(id, title, "I'm ready to fight!")
                .replyMarkup(
                        new InlineKeyboardMarkup(
                                new InlineKeyboardButton(PROCESSING_LABEL).callbackData(callbackData)
                        )
                );
    }
}
