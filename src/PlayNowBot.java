import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.abilitybots.api.objects.ReplyFlow;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static org.telegram.abilitybots.api.objects.Flag.MESSAGE;
import static org.telegram.abilitybots.api.objects.Flag.REPLY;
import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Locality.GROUP;
import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.ADMIN;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

public class PlayNowBot extends AbilityBot {
    private long adminChatId;

    public static final String BOT_TOKEN = "1023104342:AAHxVpFvoGnEEzNPf41L_QFJD2cPtTIZB94";
    public static final String BOT_USERNAME = "PlayNowBot";
    public List<Game> games = new ArrayList<>();

    public PlayNowBot() {
        super(BOT_TOKEN, BOT_USERNAME);
    }

    PlayNowBot(String botToken, String botName, long adminChatId) {
        super(botToken, botName);
        this.adminChatId = adminChatId;
    }

    public Ability sayHelloWorld() {
        return Ability
                .builder()
                .name("hello")
                .info("says hello world!")
                .locality(ALL)
                .privacy(PUBLIC)
                .action(ctx -> silent.send("Hello world!", ctx.chatId()))
                .build();
    }

    public Ability startGame() {
        return Ability
                .builder()
                .name("startgame")
                .info("Start a Game.")
                .locality(GROUP)
                .privacy(ADMIN)
                .action(ctx -> {
                            SendMessage sendMessagerequest = new SendMessage();
                            long id = ctx.chatId();
                            sendMessagerequest.setChatId(Long.toString(id));
                            sendMessagerequest.setText("Das Spiel beginnt!");
                            silent.execute(sendMessagerequest);
                            Objects.requireNonNull(getGame(id)).play();
                        }
                )
                .build();

    }

    public Ability startPlayer() {
        String frage = "Wie willst Du heißen?";
        return Ability
                .builder()
                .name("start")
                .info("Logs you into a Game.")
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> {
                            SendMessage sendMessagerequest = new SendMessage();
                            sendMessagerequest.setChatId(ctx.chatId().toString());
                            Game game = getGame(Long.parseLong(ctx.firstArg()));
                            System.out.println("recieved start on game: " + game + " vs games: " + games);
                            sendMessagerequest.setText("Du wurdest in die Gruppe (" + game.getName() + ") eingeloggt.");
                            silent.execute(sendMessagerequest);

                            Player player = new Player(ctx.chatId(), game);
                            player.setSilent(silent);


                            sendMessagerequest.setText(frage);
                            sendMessagerequest.setReplyMarkup(new ForceReplyKeyboard());
                            silent.execute(sendMessagerequest);
                        }
                )
                .reply(upd -> {
                            // Prints to console
                            System.out.println("New Player: " + upd.getMessage().getText());
                            // Sends message
                            long chatId = upd.getMessage().getChatId();
                            String name = upd.getMessage().getText();
                            Player player = Player.getPlayer(chatId);
                            player.setName(name);


                        },
                        MESSAGE,
                        REPLY,
                        isReplyToBot(),
                        isReplyToMessage(frage))
                .build();


    }

    public Ability createGame() {
        String frage = "Wie Soll das Spiel heißen?";
        return Ability
                .builder()
                .name("create")
                .info("Prepares a Game.")
                .locality(GROUP)
                .privacy(ADMIN)
                .action(ctx -> {
                            SendMessage sendMessagerequest = new SendMessage();

                            sendMessagerequest.setChatId(ctx.chatId().toString());
                            sendMessagerequest.setText(frage);
                            Game game = new Game(ctx.chatId());
                            game.setSilent(silent);
                            games.add(game);
                            silent.forceReply(frage, ctx.chatId());
                        }
                )
                .reply(upd -> {
                            // Prints to console
                            System.out.println("New Game: " + upd.getMessage().getText());
                            // Sends message
                            silent.send("New Game prepared: " + upd.getMessage().getText(), upd.getMessage().getChatId());
                            getGame(upd.getMessage().getChatId()).setName(upd.getMessage().getText());
                        },
                        MESSAGE,
                        REPLY,
                        isReplyToBot(),
                        isReplyToMessage(frage))
                .build();

    }

    public Ability invite() {
        return Ability
                .builder()
                .name("invite")
                .info("Invites Players to a Game.")
                .locality(GROUP)
                .privacy(ADMIN)
                .action(ctx -> {
                            SendMessage sendMessagerequest = new SendMessage();

                            sendMessagerequest.setChatId(ctx.chatId().toString());
                            sendMessagerequest.setText("Wer hat Lust auf eine Runde Tempel des Schreckens im Spiel " +
                                    getGame(ctx.chatId()).getName() + "?");

                            ReplyKeyboard replyKeyboard = new InlineKeyboardMarkup();
                            List<InlineKeyboardButton> rowInline = new ArrayList<>();
                            rowInline.add(new InlineKeyboardButton().setText("Bin dabei!").setUrl("https://t.me/PlayNowBot?start=" + ctx.chatId()));
                            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                            rowsInline.add(rowInline);
                            InlineKeyboardMarkup inlineKeyboardMarkup = ((InlineKeyboardMarkup) replyKeyboard);
                            inlineKeyboardMarkup.setKeyboard(rowsInline);
                            sendMessagerequest.setReplyMarkup(inlineKeyboardMarkup);

                            silent.execute(sendMessagerequest);
                        }
                )
                .build();
    }

    private Game getGame(long id) {
        for (Game game : games) {
            if (game.getId() == id)
                return game;
        }
        return null;
    }

    @Override
    public int creatorId() {
        return Math.toIntExact(adminChatId);
    }

    private Predicate<Update> isReplyToMessage(String message) {
        return upd -> {
            Message reply = upd.getMessage().getReplyToMessage();
            return reply.hasText() && reply.getText().equalsIgnoreCase(message);
        };
    }

    private Predicate<Update> isReplyToBot() {
        return upd -> upd.getMessage().getReplyToMessage().getFrom().getUserName().equalsIgnoreCase(getBotUsername());
    }
}