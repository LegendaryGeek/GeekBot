package bot.modules.octopi.events;

import bot.GeekBot;
import bot.modules.octopi.PrinterEnum;
import bot.modules.octopi.PrinterUtilities;
import bot.modules.rest.RestUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.net.ConnectException;

/**
 * Thread to monitor status of Octopi-print servers to see if the printer has completed it's job
 *
 * @author Robin "Darkguardsman" Seifert
 */
@Component
@Scope("prototype")
public class ThreadPrinterStateMonitor extends Thread
{
    //TODO convert this thread into a listener that receives a message from the printers and just outputs to discord

    private static final Logger logger = LogManager.getLogger();
    private static final String JSON_STATE_KEY = "state";
    private static final String JSON_OPERATIONAL_VALUE = "Operational";
    private static final String JSON_PRINTING_VALUE = "Printing";

    private static final String CHANNEL_OUTPUT = " <@&807485697841299457> printer %s is completed it's job";    //TODO move to a config
    private static final long OUTPUT_CHANNEL_ID = 763350428296413215l; //TODO move to a config
    private static final long REFRESH_TIMER = 10000; //TODO move to a config

    public boolean running = true;

    public final Map<PrinterEnum, String> printerState = new HashMap();

    public ThreadPrinterStateMonitor()
    {
        setDaemon(true);
    }

    @Override
    @Async
    public void run()
    {
        logger.info("Printer state monitor thread started");

        //Run until killed, will sleep between runs
        while (running)
        {
            //Loop all printers
            Stream.of(PrinterEnum.values()).forEach(this::checkPrinter);

            //Busy-wait
            try
            {
                sleep(REFRESH_TIMER);
            }
            catch (InterruptedException e)
            {
                logger.catching(e);
            }
        }

        logger.info("Printer state monitor thread stopped");
    }

    private void checkPrinter(PrinterEnum printer) {
        final String newState = gatherPrinterState(printer);
        final String lastState = printerState.getOrDefault(printer, "unknown");

        //If we go from printing to operation then we are done
        if (newState.equals(JSON_OPERATIONAL_VALUE) && lastState.equals(JSON_PRINTING_VALUE))
        {
            sendMessage(printer);
        }

        //Store current state for next iteration
        printerState.put(printer, newState);
    }

    private void sendMessage(PrinterEnum printer) {
        //Format output message
        final String formattedOutput = String.format(CHANNEL_OUTPUT, printer);

        //Send message to defined channel
        final TextChannel channel = GeekBot.getClient().getTextChannelById(OUTPUT_CHANNEL_ID);
        if (channel != null)
        {
            channel.sendMessage(formattedOutput)
                    .embed(PrinterUtilities.createPrinterOutput(printer).build())
                    .submit();
        }
        else
        {
            logger.error("Failed to get channel for printer output");
        }
    }

    /**
     * Gathers the status of the printer
     *
     * @param printer being accessed
     * @return state
     */
    @Nonnull
    private String gatherPrinterState(PrinterEnum printer)
    {
        try
        {
            final String response = RestUtil.get(printer.getUrl() + "/api/job?apikey=" + printer.getKey());
            if (response != null)
            {
                final JsonObject json = JsonParser.parseString(response).getAsJsonObject();
                return json.get(JSON_STATE_KEY).getAsString();
            }
            return "no-response";
        }
        catch (UnknownHostException | ConnectException e1)
        {
            return "offline";
        }
        catch (Exception e)
        {
            logger.catching(e);
        }
        return "error";
    }
}
