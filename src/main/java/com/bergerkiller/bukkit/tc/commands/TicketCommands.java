package com.bergerkiller.bukkit.tc.commands;

import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.bergerkiller.bukkit.common.MessageBuilder;
import com.bergerkiller.bukkit.common.utils.StringUtil;
import com.bergerkiller.bukkit.tc.Localization;
import com.bergerkiller.bukkit.tc.Permission;
import com.bergerkiller.bukkit.tc.commands.parsers.TicketParser;
import com.bergerkiller.bukkit.tc.exception.command.NoTicketSelectedException;
import com.bergerkiller.bukkit.tc.tickets.TCTicketDisplay;
import com.bergerkiller.bukkit.tc.tickets.Ticket;
import com.bergerkiller.bukkit.tc.tickets.TicketStore;

import cloud.commandframework.CommandManager;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.InitializationMethod;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import io.leangen.geantyref.TypeToken;

public class TicketCommands {

    @InitializationMethod
    private void init(AnnotationParser<CommandSender> parser, CommandManager<CommandSender> manager) {
        // Parses name -> Ticket
        manager.getParserRegistry().registerParserSupplier(TypeToken.get(Ticket.class), (params) -> {
            return new TicketParser();
        });

        // Injects the ticket the sender is editing, otherwise fails
        manager.registerExceptionHandler(NoTicketSelectedException.class, (sender, ex) -> {
            Localization.COMMAND_TICKET_NOTEDITING.message(sender);
        });
        parser.getParameterInjectorRegistry().registerInjector(Ticket.class, (context, annot) -> {
            if (!(context.getSender() instanceof Player)) {
                throw new InvalidCommandSenderException(
                        context.getSender(),
                        Player.class,
                        Collections.emptyList());
            }
            Ticket ticket = TicketStore.getEditing((Player) context.getSender());
            if (ticket == null) {
                throw new NoTicketSelectedException();
            }
            return ticket;
        });
    }

    @CommandMethod("train list tickets")
    @CommandDescription("Lists the names of all tickets that exist")
    private void commandTrainList(
            final CommandSender sender
    ) {
        commandList(sender);
    }

    //@ProxiedBy("train list tickets")
    @CommandMethod("train ticket list")
    @CommandDescription("Lists the names of all tickets that exist")
    private void commandList(
            final CommandSender sender
    ) {
        MessageBuilder builder = new MessageBuilder();
        builder.yellow("The following tickets are available:");
        builder.newLine().setSeparator(ChatColor.WHITE, " / ");
        for (Ticket ticket : TicketStore.getAll()) {
            builder.green(ticket.getName());
        }
        builder.send(sender);
    }

    @CommandMethod("train ticket edit <name>")
    @CommandDescription("Edits a ticket by name")
    private void commandEdit(
              final Player sender,
              final @Argument("name") Ticket ticket
    ) {
        Permission.TICKET_MANAGE.handle(sender);

        TicketStore.setEditing(sender, ticket);
        sender.sendMessage(ChatColor.GREEN + "You are now editing ticket " + ChatColor.YELLOW + ticket.getName());
    }

    @CommandMethod("train ticket create")
    @CommandDescription("Creates a new ticket with a unique random name")
    private void commandCreate(
              final Player sender
    ) {
        Permission.TICKET_MANAGE.handle(sender);

        Ticket newTicket = TicketStore.createTicket(TicketStore.DEFAULT);
        sender.sendMessage(ChatColor.GREEN + "You have created a new ticket with the name " + ChatColor.YELLOW + newTicket.getName());
        TicketStore.setEditing(sender, newTicket);
    }

    @CommandMethod("train ticket create <name>")
    @CommandDescription("Creates a new ticket with a name as specified")
    private void commandCreateWithName(
              final Player sender,
              final @Argument("name") String name
    ) {
        Permission.TICKET_MANAGE.handle(sender);

        Ticket newTicket = TicketStore.createTicket(TicketStore.DEFAULT, name);
        sender.sendMessage(ChatColor.GREEN + "You have created a new ticket with the name " + ChatColor.YELLOW + newTicket.getName());
        TicketStore.setEditing(sender, newTicket);
    }

    @CommandMethod("train ticket give <ticket> <players>")
    @CommandDescription("Gives a ticket by name to one or more players")
    private void commandGiveTicket(
              final CommandSender sender,
              final @Argument("ticket") String ticketName,
              final @Argument("players") String[] playerNames
    ) {
        Permission.TICKET_MANAGE.handle(sender);

        Ticket ticket = TicketStore.getTicket(ticketName);
        if (ticket == null) {
            sender.sendMessage(ChatColor.RED + "Failed to give ticket: ticket with name " + ticketName + " does not exist!");
            return;
        }

        for (String playerName : playerNames) {
            Player player = Bukkit.getPlayer(playerName); //eh?
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Failed to give ticket to player " + playerName + ": not online");
            } else {
                ItemStack item = ticket.createItem(player);
                player.getInventory().addItem(item);
                sender.sendMessage(ChatColor.GREEN + "Ticket " + ChatColor.YELLOW + ticket.getName() + ChatColor.GREEN + 
                        " given to player " + ChatColor.YELLOW + player.getName());
            }
        }
    }

    @CommandMethod("train ticket clone")
    @CommandDescription("Clones the currently edited ticket with a random new name")
    private void commandCloneTicket(
              final Player sender,
              final Ticket ticket
    ) {
        Permission.TICKET_MANAGE.handle(sender);

        Ticket newTicket = TicketStore.createTicket(ticket);
        sender.sendMessage(ChatColor.GREEN + "You cloned the ticket, creating a new ticket with the name " + ChatColor.YELLOW + newTicket.getName());
        TicketStore.setEditing(sender, newTicket);
    }

    @CommandMethod("train ticket clone <newname>")
    @CommandDescription("Clones the currently edited ticket with the new name specified")
    private void commandCloneTicketWithNewName(
              final Player sender,
              final Ticket ticket,
              final @Argument("newname") String newTicketName
    ) {
        Permission.TICKET_MANAGE.handle(sender);

        Ticket newTicket = TicketStore.createTicket(ticket, newTicketName);
        if (newTicket == null) {
            sender.sendMessage(ChatColor.RED + "Failed to clone ticket: a ticket with the name " + newTicketName + " already exists");
            return;
        }
        sender.sendMessage(ChatColor.GREEN + "You cloned the ticket, creating a new ticket with the name " + ChatColor.YELLOW + newTicket.getName());
        TicketStore.setEditing(sender, newTicket);
    }

    @CommandMethod("train ticket rename <newname>")
    @CommandDescription("Renames the currently edited ticket")
    private void commandRenameTicket(
              final Player sender,
              final Ticket ticket,
              final @Argument("newname") String newTicketName
    ) {
        Permission.TICKET_MANAGE.handle(sender);

        if (ticket.setName(newTicketName)) {
            sender.sendMessage(ChatColor.GREEN + "Ticket has been renamed to " + ChatColor.YELLOW + ticket.getName());
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to rename ticket to " + newTicketName + ": a ticket with this name already exists!");
        }
    }

    @CommandMethod("train ticket realm <newrealm>")
    @CommandDescription("Changes the realm of the currently edited ticket")
    private void commandSetRealm(
              final Player sender,
              final Ticket ticket,
              final @Argument("newrealm") String newRealm
    ) {
        Permission.TICKET_MANAGE.handle(sender);

        ticket.setRealm(newRealm);
        TicketStore.markChanged();
        sender.sendMessage(ChatColor.GREEN + "Ticket realm set to " + ChatColor.YELLOW + newRealm);
    }

    @CommandMethod("train ticket background|image")
    @CommandDescription("Reads what background image is configured for the currently edited ticket")
    private void commandSetBackground(
              final Player sender,
              final Ticket ticket
    ) {
        if (ticket.getBackgroundImagePath().isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No background image is set for this ticket (default).");
            sender.sendMessage(ChatColor.YELLOW + "To set a background image, use /train ticket background [path]");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Background image is currently set to: " + ChatColor.WHITE + ticket.getBackgroundImagePath());
            sender.sendMessage(ChatColor.YELLOW + "To set a background image, use /train ticket background [path]");
        }
    }

    @CommandMethod("train ticket background|image <newimage>")
    @CommandDescription("Configures a custom background image for the currently edited ticket")
    private void commandSetBackground(
              final Player sender,
              final Ticket ticket,
              final @Argument("newimage") String newImage
    ) {
        Permission.TICKET_MANAGE.handle(sender);

        ticket.setBackgroundImagePath(newImage);
        TicketStore.markChanged();
        if (newImage.isEmpty()) {
            sender.sendMessage(ChatColor.GREEN + "Ticket background image reset to the default image");
        } else {
            sender.sendMessage(ChatColor.GREEN + "Ticket background image set to " + ChatColor.YELLOW + newImage);
        }

        // Redraw the ticket on active displays
        for (TCTicketDisplay display : TCTicketDisplay.getAllDisplays(TCTicketDisplay.class)) {
            if (TicketStore.getTicketFromItem(display.getMapItem()) == ticket) {
                display.renderBackground();
            }
        }
    }

    @CommandMethod("train ticket maximumuses|maxuses|uselimit unlimited|infinite")
    @CommandDescription("Sets the number of uses for the currently edited ticket to unlimited")
    private void commandSetUnlimitedMaximumUses(
              final Player sender,
              final Ticket ticket
    ) {
        commandSetMaximumUses(sender, ticket, -1);
    }

    @CommandMethod("train ticket maximumuses|maxuses|uselimit <newmaxuses>")
    @CommandDescription("Sets the number of uses for the currently edited ticket")
    private void commandSetMaximumUses(
              final Player sender,
              final Ticket ticket,
              final @Argument("newmaxuses") int newMaximumUses
    ) {
        Permission.TICKET_MANAGE.handle(sender);

        ticket.setMaxNumberOfUses(newMaximumUses);
        TicketStore.markChanged();
        if (newMaximumUses >= 0) {
            sender.sendMessage(ChatColor.GREEN + "Ticket maximum number of uses set to " + ChatColor.YELLOW +
                    newMaximumUses);
        } else {
            sender.sendMessage(ChatColor.GREEN + "Ticket now has unlimited number of uses");
        }
    }

    @CommandMethod("train ticket destination <newdestination>")
    @CommandDescription("Sets a destination to apply to the train when the currently edited ticket is used")
    private void commandSetDestination(
              final Player sender,
              final Ticket ticket,
              final @Argument("newdestination") String newDestination
    ) {
        Permission.TICKET_MANAGE.handle(sender);

        ticket.getProperties().set("destination", newDestination);
        sender.sendMessage(ChatColor.GREEN + "Ticket destination set to " + ChatColor.YELLOW + newDestination);
    }

    @CommandMethod("train ticket tags [newtags]")
    @CommandDescription("Sets tags to apply to the train when the currently edited ticket is used")
    private void commandSetTags(
              final Player sender,
              final Ticket ticket,
              final @Argument("newtags") String[] newTags
    ) {
        Permission.TICKET_MANAGE.handle(sender);

        if (newTags == null || newTags.length == 0) {
            ticket.getProperties().set("tags", new String[0]);
            sender.sendMessage(ChatColor.GREEN + "All ticket tags have been cleared");
        } else {
            ticket.getProperties().set("tags", newTags);
            sender.sendMessage(ChatColor.GREEN + "Ticket tags set: " + ChatColor.YELLOW + StringUtil.combineNames(newTags));
        }
    }
}
