import org.omg.CORBA.Any;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.*;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.telegram.abilitybots.api.objects.Flag.MESSAGE;
import static org.telegram.abilitybots.api.objects.Flag.REPLY;
import static org.telegram.abilitybots.api.objects.Locality.*;
import static org.telegram.abilitybots.api.objects.Privacy.*;

public class PlayNowBot extends AbilityBot {
    private long adminChatId;

    private static final String BOT_TOKEN = "1023104342:AAHxVpFvoGnEEzNPf41L_QFJD2cPtTIZB94";
    private static final String BOT_USERNAME = "PlayNowBot";

    private List<Game> games = new ArrayList<>();
    private EmojiSet selectedTexture = EmojiSet.CORONA;



    public PlayNowBot() {
        super(BOT_TOKEN, BOT_USERNAME);
    }

    PlayNowBot(String botToken, String botName, long adminChatId) {
        super(botToken, botName);
        this.adminChatId = adminChatId;
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
                            setNewGame(ctx, null);
                        }
                        Game game = getGame(ctx.chatId());
                        if(!game.isRunning())
                            inviteRoutine(ctx);
                        }
                ).build();
    }

    private void inviteRoutine(MessageContext ctx){
        Game game = getGame(ctx.chatId());
        MyMessage sendMessagerequest = new MyMessage(ctx.chatId(),silent);
        updateLobby(sendMessagerequest,game);
    }

    private void setNewGame(MessageContext ctx, String alternateName){
        Game game = new Game(ctx.chatId(), selectedTexture, this);
        game.setSilent(silent);
        games.add(game);
        if (ctx.arguments().length == 0) {
            if(alternateName!=null)
            game.setName(alternateName);
            else
                game.setName("Tempel des Schreckens");
        } else {
            game.setName(ctx.firstArg());
        }
    }


    public Reply replyToQuery() {

        Consumer<Update> action = upd -> {
            CallbackQuery query = upd.getCallbackQuery();
            Game game = getGame(query.getMessage().getChatId());
            String[] data = query.getData().split(":");
            if(game.findPlayer(upd.getMessage().getFrom().getId()) == null){
                MyMessage message = new MyMessage(upd.getCallbackQuery().getMessage(), this);
                message.setText("Du kannst leider nur zusehen, weil Du nicht mitspielst.");
                message.send();
                return;
            }

            if(data[0].equalsIgnoreCase("texture")){
                MyMessage message = new MyMessage(upd.getCallbackQuery().getMessage(), this);
                for (EmojiSet set:EmojiSet.values()) {
                    if(set.toString().equalsIgnoreCase(data[1]))
                        if (upd.getCallbackQuery().getMessage().isUserMessage()) {
                            this.selectedTexture = set;
                            message.setText("Default-Aussehen geändert zu " + this.selectedTexture.toString());

                        }else if(getGame(upd.getCallbackQuery().getMessage().getChatId()) != null) {
                            game = getGame(upd.getCallbackQuery().getMessage().getChatId());
                            game.setTexture(set);
                            message.setText("Aussehen geändert zu " + game.texture.toString());
                        }
                }
                message.send();
                return;
            }

            if(game.isRunning()) {
                if (isLobbyQuery(query)) {
                    sendAlarm("There is a game running already.",query,false);
                    System.out.println("received lobby query during running game");
                } else {
                    replyToGameQuery(query);
                }
            }else{
                if (isLobbyQuery(query)) {
                    replyToLobbyQuery(query);
                } else {
                    sendAlarm("There is no game running at the moment.",query,false);
                    System.out.println("received game query with no game running: "+query.getData());
                }
            }
        };
        return Reply.of(action, Flag.CALLBACK_QUERY);
    }

    private void replyToGameQuery(CallbackQuery query){
        long chatId = query.getMessage().getChatId();
        Game game = getGame(chatId);
        long pusher = query.getFrom().getId();
        Player player = game.findPlayer(pusher);

        String[] data = query.getData().split(";");
        long chosenId = Long.parseLong(data[0]);
        int cardIndex = Integer.parseInt(data[1]);
        Player chosenOne = game.findPlayer(chosenId);

        String reply = null;
        boolean alert =false;

        if(chosenOne.getCards().isEmpty()){
            alert = false;
            reply = chosenOne.getName() + " hat keine Karten mehr.";
        }
        if(!chosenOne.knowsHisCards){
            alert = true;
            reply = "Dieser Spieler kennt Seine Karten noch nicht.";
        }
        if (chosenOne.getCards().isExposed(cardIndex)){
            alert = false;
            reply = "Diese Karte wurde schon geöffnet.";
        }

        if(chosenOne instanceof  DummyPlayer){
            if (cardIndex==-1) {
                if(game.getActivePlayer() instanceof DummyPlayer || (game.getActivePlayer()==player)){

                } else {
                    alert = true;
                    reply = "Die Rolle vom Bot: " + chosenOne.getRole().getEmoji(game);
                }
            } else {
                alert = true;
                reply = "Die Karten von "+chosenOne.getName()+":\r\n"+chosenOne.getCards().getHidden().print();
                chosenOne.knowsHisCards=true;
                game.printStatsWithKeyboard(query.getMessage());
            }
        } else {
            if (chosenOne==player && cardIndex==-1) {
                if(game.getActivePlayer() instanceof DummyPlayer){

                } else {
                    alert = true;
                    reply = "Deine Rolle:" + player.getRole().getEmoji(game);
                }
            } else if (game.getActivePlayer().getId() != player.getId()) {
                alert = false;
                reply = "Du bist nicht am Zug.";
            }
        }

        if(chosenOne==player && cardIndex!=-1){
            alert = true;
            reply = "Deine Karten:\r\n" + player.getCards().getHidden().printSort();
            //System.out.println("told "+player.getName()+" his cards: "
            //        + player.getCards().print() + " and hidden sorted: " + player.getCards().getHidden().printSort());
            player.knowsHisCards = true;
            game.printStatsWithKeyboard(query.getMessage());
        }


        if (reply==null) {
            Message message = query.getMessage();
            game.nextMove(chosenOne, cardIndex, message);
        }else{
            sendAlarm(reply,query,alert);
        }
    }
    private void replyToLobbyQuery(CallbackQuery query) {
        long chatId = query.getMessage().getChatId();
        Game game = getGame(chatId);
        MyMessage lobby = new MyMessage(query.getMessage(),this);
        if (query.getData().equalsIgnoreCase("joinrequest")){
            User user = query.getFrom();
            if(game.findPlayer(user.getId()) == null){
                Player tojoin = new Player(user.getId(),user.getUserName(), game);
                if (tojoin.getName()!=null) {
                    game.addPlayer(tojoin);
                } else {
                    sendAlarm("Du kannst leider nur Beitreten, indem Du mit Deinem Wunschnamen auf die Lobby antwortest.",query,true);
                }
            } else {
                game.removeplayer(user.getId());
            }
            updateLobby(lobby,game);

        } else if(query.getData().equalsIgnoreCase("startgame")){
            if (game.findPlayer(query.getFrom().getId()) != null){
                closeLobbyOnStart(lobby,game);
                game.play();
            }else{
                sendAlarm("Du kannst das Spiel nur starten, wenn Du selbst in der lobby bist.",query,true);

            }
        } else if(query.getData().equalsIgnoreCase("cancel")) {
            if(game.findPlayer(query.getFrom().getId())!= null) {
                removeGame(game);
                MyMessage toremove = new MyMessage(query.getMessage(), this);
                toremove.setText("Lobby geschlossen.");
                toremove.send();
            } else {
                sendAlarm("You must be in the Lobby to close it.",query,true);
            }
        } else {
            System.out.println("unhandeled Joinrequest: "+query.getData());
        }
    }

    private boolean isLobbyQuery(CallbackQuery query){
        String parameter = query.getData();
        return parameter.equalsIgnoreCase("joinrequest") || parameter.equalsIgnoreCase("startgame") || parameter.equalsIgnoreCase("cancel");
    }

    private void sendAlarm(String message, CallbackQuery query, boolean alert){
        AnswerCallbackQuery reply = new AnswerCallbackQuery();
        reply.setCallbackQueryId(query.getId());
        reply.setShowAlert(alert);
        reply.setText(message);
        try {
            this.execute(reply);
        } catch (TelegramApiException e) {
            if (e.toString().contains("[400]")) {
                //silent.send("Sorry, I had a timeout and had to discard your Buttonpush.", query.getMessage().getChatId());
                System.out.println(e.toString());
            }else
                System.err.println(e.toString());
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

    public Ability setName() {
        return Ability
                .builder()
                .name("name")
                .info("Takes one argument to let you set your nickname.")
                .locality(ALL)
                .privacy(PUBLIC)
                .action(ctx -> {
                    MyMessage message = new MyMessage(ctx.user().getId(),silent);

                    try{
                        if(ctx.arguments().length >0) {
                            Player.addName(ctx.firstArg(), ctx.user().getId());
                            message.setText("Dein Name wurde zu *" + ctx.firstArg() + "* geändert.");
                        }else{
                            message.setText("Du Musst mir Deinen Namen als Argument übergeben, um ihn zu setzen. Also: /name [username]");
                        }
                    } catch (Exception e){
                        System.out.println(e.getMessage());
                    }

                    message.send();

                        }
                )
                .build();
    }

    public Ability startUser() {
        return Ability
                .builder()
                .name("start")
                .info("Method called when users open bot.")
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> {
                    MyMessage message = new MyMessage(ctx.chatId(),silent);
                    String name = Player.getName(ctx.user().getId());
                    if (name!=null)
                        message.setText("Welcome back to the Playnowbot, "+name+"\r\n"
                                +"to change your name just hit /name [nickname]\r\n"+
                                "to let me moderate a game with your friends add me to a group and hit /invite.");
                    else
                        message.setText("Welcome to the Playnowbot.\r\n"
                                +"to change your name just hit /name [nickname] \r\n" +
                                "to let me moderate a game with your friends add me to a group and hit /invite.");
                    message.send();
                }
                )
                .build();
    }

    public Ability startGame() {
        return Ability
                .builder()
                .name("startgame")
                .info("(re-)starts a Game.")
                .locality(GROUP)
                .privacy(GROUP_ADMIN)
                .action(ctx -> {
                            Objects.requireNonNull(getGame(ctx.chatId())).play();
                        }
                )
                .build();
    }

    public Ability addBot() {
        return Ability
                .builder()
                .name("addbot")
                .info("Adds a Bot to the Game.")
                .locality(GROUP)
                .privacy(GROUP_ADMIN)
                .action(ctx -> {
                    Game game = getGame(ctx.chatId());
                    Player bot = new DummyPlayer(game);
                    game.addPlayer(bot);
                    game.sendMarkdown("Added Bot: "+ bot.getName());
                        }
                )
                .build();
    }

    public Ability resetGame() {
        return Ability
                .builder()
                .name("reset")
                .info("Resets the Bot in one chat.")
                .locality(GROUP)
                .privacy(GROUP_ADMIN)
                .action(ctx -> {
                            Game game = getGame(ctx.chatId());
                            String oldName = null;
                            if (game != null) {
                                game.sendMarkdown("Spiel wurde zurückgesetzt.");
                                removeGame(game);
                                oldName = game.getName();
                            }
                            setNewGame(ctx,oldName);
                            inviteRoutine(ctx);
                        }
                )
                .build();
    }

    public Ability lobby() {
        return Ability
                .builder()
                .name("lobby")
                .info("(re-)opens Lobby.")
                .locality(GROUP)
                .privacy(GROUP_ADMIN)
                .action(ctx -> {
                    Game game = getGame(ctx.chatId());
                    String oldName = null;
                    if (game == null) {
                        setNewGame(ctx,null);
                    }
                    game=getGame(ctx.chatId());
                    game.interrupt();
                    inviteRoutine(ctx);
                }
                )
                .build();
    }

    public Ability texture() {
        return Ability
                .builder()
                .name("texture")
                .info("Lets you choose a Texturepack for the bot.")
                .locality(ALL)
                .privacy(GROUP_ADMIN)
                .action(ctx -> {
                    List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                    for (EmojiSet set: EmojiSet.values() ) {
                        List<InlineKeyboardButton> rowInline = new ArrayList<>();
                        rowInline.add(new InlineKeyboardButton()
                                .setText(set.toString() + ": " + set.adventurer() + set.guard()
                                        + set.gold() + set.fire() + set.empty()+set.closed()+set.key())
                                .setCallbackData("texture:"+set.toString()));
                        rowsInline.add(rowInline);
                    }
                    MyMessage message = new MyMessage(ctx.chatId(),silent);
                    message.setText("Wähle bitte ein Aussehen:");
                    ReplyKeyboard replyKeyboard = new InlineKeyboardMarkup();
                    InlineKeyboardMarkup inlineKeyboardMarkup = ((InlineKeyboardMarkup) replyKeyboard);
                    inlineKeyboardMarkup.setKeyboard(rowsInline);
                    message.setReplyMarkup(inlineKeyboardMarkup);
                    message.send();
                        }
                ).build();
    }

    public Ability sayHelloWorld() {
        return Ability
                .builder()
                .name("hello")
                .info("says hello world! ")
                .locality(ALL)
                .privacy(ADMIN)
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
    
    public Reply replyToSticker() {
        Consumer<Update> action = upd -> {
            if (upd.getMessage().isUserMessage()) {
                System.out.println("Received Sticker: " + upd.getMessage().getSticker().getFileId());
                silent.send(upd.getMessage().getSticker().getFileId(),upd.getMessage().getChatId());
            }
        };
        return Reply.of(action, update -> update.getMessage().hasSticker());
    }

    public Reply replyToAnimation() {
        Consumer<Update> action = upd -> {
                String fileId = upd.getMessage().getAnimation().getFileId();
                System.out.println("Received Animation: " + fileId);
                silent.send("Received Animation: " + fileId,upd.getMessage().getChatId());
                getGame(upd.getMessage().getChatId()).sendSticker(fileId);
        };
        return Reply.of(action, update -> update.getMessage().hasAnimation());
    }

    public Ability translateUni() {
        return Ability
                .builder()
                .name("uni")
                .info("translates Unicode to emoji!")
                .locality(Locality.USER)
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
                    silent.execute(sendMessagerequest);
                })
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

    private Predicate<Update> isReplyToBot() {
        return upd -> upd.getMessage().getReplyToMessage().getFrom().getUserName().equalsIgnoreCase(getBotUsername());
    }

    public void removeGame(Game toRemove) {
        games.remove(toRemove);
    }

    public void updateLobby(MyMessage lobby, Game game){
        lobby.setText("Tempel des Schreckens ["+game.texture+"-Edition].\r\n"+
                "Lobby "+game.getName()+":\r\n");
        lobby.append(game.printPlayers());
        lobby.setReplyMarkup(getJoinKeyboard(game.numPlayers()));
        lobby.send();
    }

    public InlineKeyboardMarkup getJoinKeyboard(int numPlayers) {
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        rowInline1.add(new InlineKeyboardButton().setText("Beitreten/Verlassen").setCallbackData("joinrequest"));
        rowInline1.add(new InlineKeyboardButton().setText("Lobby schließen").setCallbackData("cancel"));
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(rowInline1);
        if(numPlayers>2 && numPlayers <11){
            List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
            rowInline2.add(new InlineKeyboardButton().setText("Spiel starten!").setCallbackData("startgame"));
            rowsInline.add(rowInline2);
        }
        
        ReplyKeyboard replyKeyboard = new InlineKeyboardMarkup();
        InlineKeyboardMarkup inlineKeyboardMarkup = ((InlineKeyboardMarkup) replyKeyboard);
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        return inlineKeyboardMarkup;
    }

    private void closeLobbyOnStart(MyMessage lobby, Game game){
        lobby.setText("Tempel des Schreckens ["+game.texture+"-Edition] gestartet.\r\n"+
                "Es spielen mit:\r\n");
        lobby.append(game.printPlayers());
        lobby.send();
    }
}