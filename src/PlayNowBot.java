import jdk.nashorn.internal.codegen.CompilerConstants;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.*;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
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
                .info("says hello world! ")
                .locality(ALL)
                .privacy(PUBLIC)
                .action(ctx -> {
                    SendMessage sendMessagerequest = new SendMessage();
                    sendMessagerequest.setChatId(ctx.chatId());
                    sendMessagerequest.enableMarkdown(true);
                    //sendMessagerequest.setReplyMarkup(new ForceReplyKeyboard());
                    String arg = "";
                    if(ctx.arguments().length>0){
                        arg = ctx.firstArg();
                    }
                    sendMessagerequest.setText("*Hello* _world_ ! "+arg);
                    System.out.println("I greeted "+ctx.user().getId()+" in chat "+ctx.chatId()+"!");
                    silent.execute(sendMessagerequest);
                })
                .build();
    }

    public Ability translateUni() {
        return Ability
                .builder()
                .name("uni")
                .info("translates to Unicode! ")
                .locality(ALL)
                .privacy(PUBLIC)
                .action(ctx -> {
                    SendMessage sendMessagerequest = new SendMessage();
                    sendMessagerequest.setChatId(ctx.chatId());
                    sendMessagerequest.enableMarkdown(true);

                    String str = ctx.firstArg().split(" ")[0];
                    str = str.replace("\\","");
                    String[] arr = str.split("u");
                    String text = "";
                    for(int i = 1; i < arr.length; i++){
                        int hexVal = Integer.parseInt(arr[i], 16);
                        text += (char)hexVal;
                    }

                    sendMessagerequest.setText("Translated: "+text);
                    System.out.println("I greeted "+ctx.user().getId()+" in chat "+ctx.chatId()+"!");
                    silent.execute(sendMessagerequest);
                })
                .build();
    }


    public Ability resetGame() {
        return Ability
                .builder()
                .name("reset")
                .info("Resets the Bot in one chat.")
                .locality(GROUP)
                .privacy(ADMIN)
                .action(ctx -> {
                            Game game = getGame(ctx.chatId());
                            game.sendMarkdown("Spiel wurde zurückgesetzt.");
                            removeGame(game);
                        }
                )
                .build();
    }

    public Reply replyToQuery() {

        Consumer<Update> action = upd -> {
            CallbackQuery query = upd.getCallbackQuery();
            System.out.println("Received Query: "+query.getData());
            Game game = getGame(query.getMessage().getChatId());
            if(game.isRunning()) {
                replyToGameQuery(query);
            }else{
                replyToLobbyQuery(query);
            }
        };
        return Reply.of(action, Flag.CALLBACK_QUERY);
    }

    private void replyToGameQuery(CallbackQuery query){
        long pusher = query.getFrom().getId();
        Player player = getPlayer(pusher);
        long chatId = query.getMessage().getChatId();
        Game game = getGame(chatId);
        String[] data = query.getData().split(";");
        long chosenId = Long.parseLong(data[0]);
        int cardIndex = Integer.parseInt(data[1]);
        Player chosenOne = getPlayer(chosenId);

        AnswerCallbackQuery reply = new AnswerCallbackQuery();
        reply.setCallbackQueryId(query.getId());


        if(chosenOne.getCards().isEmpty()){
            reply.setShowAlert(false);
            reply.setText(chosenOne.getName() + " hat keine Karten mehr.");
        }
        if(!chosenOne.knowsHisCards){
            reply.setShowAlert(true);
            reply.setText("Dieser Spieler kennt Seine Karten noch nicht.");
        }
        if (chosenOne.getCards().isExposed(cardIndex)){
            reply.setShowAlert(false);
            reply.setText("Diese Karte wurde schon geöffnet.");
        }
        if (game.getActivePlayer().getId() != player.getId()) {
            reply.setShowAlert(false);
            reply.setText("Du bist nicht am Zug.");
        }
        if(chosenOne==player){
            if (cardIndex==-1) {
                reply.setShowAlert(true);
                reply.setText("Deine Rolle:" + player.getRole().getEmoji(game));
            } else {
                reply.setShowAlert(true);
                reply.setText("Deine Karten:\r\n" + player.getCards().getHidden().printSort());
                player.knowsHisCards = true;
                game.printStatsWithKeyboard(query.getMessage());
            }
        }

        try {
            this.execute(reply);
            System.out.println(reply.getText());
        } catch (TelegramApiException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        if (reply.getText()==null) {
            Message message = query.getMessage();
            game.nextMove(chosenOne, cardIndex, message);
        }

    }
    private void replyToLobbyQuery(CallbackQuery query) {
        long chatId = query.getMessage().getChatId();
        Game game = getGame(chatId);
        if (query.getData().equalsIgnoreCase("joinrequest")){
            User user = query.getFrom();
            if(game.findPlayer(user.getId()) == null){
                Player tojoin = new Player(user.getId(),user.getUserName(), game);
                if (tojoin.getName()!=null) {
                    game.addPlayer(tojoin);
                } else {
                    AnswerCallbackQuery reply = new AnswerCallbackQuery();
                    reply.setCallbackQueryId(query.getId());
                    reply.setShowAlert(true);
                    reply.setText("Du kannst leider nur Beitreten, indem Du mit Deinem Wunschnamen auf die Lobby antwortest.");
                    try {
                        this.execute(reply);
                    } catch (TelegramApiException e) {
                        System.err.println(e.getMessage());
                    }
                }
            } else {
                game.removeplayer(user.getId());
            }
            MyMessage lobby = new MyMessage(query.getMessage(),this);
            updateLobby(lobby,game);

        } else if(query.getData().equalsIgnoreCase("startgame")){
            game.play();
        } else {
            System.err.println("Unhandeled JoinQuery: "+query.getData());
        }
    }

    public Reply joinWithName(){
        Consumer<Update> action = upd -> {
            Message message = upd.getMessage();
            long chatId = message.getChatId();
            String name = message.getText();
            Game game = getGame(chatId);
            User from = message.getFrom();
            Player potentialPlayer = game.findPlayer(from.getId());
            if(potentialPlayer != null){
                potentialPlayer.setName(name);
                potentialPlayer.say("Dein Name wurde geändert zu: "+potentialPlayer.getName());
            }else{
                Player player = new Player(from.getId(),from.getUserName(), game);
                if(game.isRunning()){
                    player.say("Das Spiel läuft leider schon.");
                    return;
                }else{
                    player.setName(name);
                    game.addPlayer(player);
                    player.say("Du bist dem Spiel " + game.getName() + " als \""+name+"\" beitreten.");
                }
            }
            MyMessage lobby = new MyMessage(message.getReplyToMessage(),this);
            updateLobby(lobby,game);
        };
        return Reply.of(action, MESSAGE, REPLY, isReplyToBot());
    }

    public Reply replyToSticker() {
        Consumer<Update> action = upd -> {
            if (upd.getMessage().hasSticker()) {
                System.out.println("Received Sticker: " + upd.getMessage().getSticker().getFileId());
                silent.send(upd.getMessage().getSticker().getFileId(),upd.getMessage().getChatId());
            }
        };
        return Reply.of(action, update -> update.getMessage().hasSticker());
    }

    public Ability startGame() {
        return Ability
                .builder()
                .name("startgame")
                .info("Start a Game.")
                .locality(GROUP)
                .privacy(PUBLIC)
                .action(ctx -> {
                            Objects.requireNonNull(getGame(ctx.chatId())).play();
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
                            silent.execute(sendMessagerequest);
                            Player player = new Player(ctx.chatId(),ctx.user().getUserName(), game);
                            player.say("Du möchtest dem Spiel " + game.getName() + " beitreten.");
                            game.addPlayer(player);
                            sendMessagerequest.setText(frage);
                            sendMessagerequest.setReplyMarkup(new ForceReplyKeyboard());
                            silent.execute(sendMessagerequest);
                        }
                )
                .reply(upd -> {
                            long chatId = upd.getMessage().getChatId();
                            String name = upd.getMessage().getText();
                            Player player = getPlayer(chatId);
                            player.setName(name);
                            player.getGame().sendMarkdown("*"+player.getName()+"* ist dem Spiel beigetreten.");
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
                .privacy(PUBLIC)
                .action(ctx -> {
                            if (getGame(ctx.chatId()) == null) {
                                Game game = new Game(ctx.chatId(), EmojiSet.CORONA, this);
                                game.setSilent(silent);
                                games.add(game);
                                if (ctx.arguments().length == 0) {
                                    game.setName("Tempel des Schreckens");
                                } else if (ctx.arguments().length == 1) {
                                    game.setName(ctx.firstArg());
                                } else {
                                    game.setName(ctx.firstArg());
                                    Player dummy = new Player(12345, "Dummy", game);
                                    dummy.setName(ctx.secondArg());
                                }
                            }

                            Game game = getGame(ctx.chatId());
                            MyMessage sendMessagerequest = new MyMessage(ctx.chatId(),silent);
                            updateLobby(sendMessagerequest,game);
                        }
                ).build();
    }


    private Game getGame(long id) {
        for (Game game : games) {
            if (game.getId() == id)
                return game;
        }
        return null;
    }

    private Player getPlayer(long id) {
        for (Game game : games) {
            Player player = game.findPlayer(id);
            if (player != null)
                return player;
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

    public void removeGame(Game toRemove) {
        games.remove(toRemove);
    }

    private void updateLobby(MyMessage lobby, Game game){
        lobby.setText("Starte Tempel des Schreckens ["+game.texture+"-Edition].\r\n"+
                "Lobby "+game.getName()+":\r\n");
        lobby.append(game.printPlayers());
        lobby.setReplyMarkup(game.getJoinKeyboard());
        lobby.send();
    }
}