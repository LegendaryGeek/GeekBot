package bot.printers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jagrosh.jdautilities.command.CommandEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bot.GeekBot;
import bot.json.models.PrintJobInfo;
import net.dv8tion.jda.api.EmbedBuilder;

public class PrinterUtilities {
    
	static Gson gson = new Gson();
    private static Logger log = LogManager.getLogger();

	private PrinterUtilities(){}

    public static EmbedBuilder PrinterJob(CommandEvent event, Printer printer) {
        JsonObject json = null;
        PrintJobInfo info = null;
        Integer estimatedPrintTime = null;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        EmbedBuilder builder = new EmbedBuilder();

        try {
            json = JsonParser.parseString(GeekBot.get(printer.getUrl() + "/job?apikey=" + printer.getKey()))
                    .getAsJsonObject();
            info = gson.fromJson(json, PrintJobInfo.class);
			estimatedPrintTime = info.getJob().getEstimatedPrintTime();

		} catch (Exception e) {
			log.catching(e);
		}

		if (estimatedPrintTime != null)
			hours = estimatedPrintTime / 3600;
		if (estimatedPrintTime != null)
			minutes = (estimatedPrintTime % 3600) / 60;
		if (estimatedPrintTime != null)
			seconds = estimatedPrintTime % 60;

		if (info.getJob().getFile().getName() != null)
			builder.addField("File name", info.getJob().getFile().getName(), true);

		if (estimatedPrintTime != null)
			builder.addField("Estimaed Print Time", String.format("%02d:%02d:%02d", hours, minutes, seconds), true);

		if (info.getJob().getFilament().getLength() != null)
			builder.addField("Filament Length", info.getJob().getFilament().getLength().toString(), true);

		if ((Float)info.getJob().getFilament().getVolume() != null) 
			builder.addField("Filament Volume", info.getJob().getFilament().getVolume().toString(), true);

		if ((Float)info.getProgress().getCompletion() != null)
			builder.addField("Percent Done", info.getProgress().getCompletion().toString(), true);

		builder.addField("Printer State", info.getState(), true);
        return builder;
	}
	
    public static double fillamentPricePerLength (double diameter, double mass, double pricePerMass, double density) {
        // Parameters:
        // diameter: Fillament diameter, in millimeters (mm).
        // mass: Spool net mass/weight, in kilograms (kg).
        // pricePerMass: Cost of spool, currency agnostic. No cents.
        // density: Fillament density, in g/cm^3. Not often labelled, Simplify3D values below:
        //      PLA:  1.24 g/cm^3
        //      ABS:  1.04 g/cm^3
        //      PETG: 1.23 g/cm^3
        //      TPU:  1.21 g/cm^3 (median)
        //
        // Output is cost per millimetre, in the same unit as input (Dollars = dollars, Cents = cents)

        if (diameter <= 0) { throw new java.lang.IllegalArgumentException("diameter must be positive"); }
        if (mass <= 0) { throw new java.lang.IllegalArgumentException("mass must be positive"); }
        if (density <= 0) { throw new java.lang.IllegalArgumentException("density must be positive"); }
        if (pricePerMass <= 0) { throw new java.lang.IllegalArgumentException("pricePerMass must be positive"); }

        double volume = mass / density * 1000.0;
        double length = volume / (java.lang.Math.PI*java.lang.Math.pow(diameter / 2.0, 2)) * 1000.0;
        double pricePerLength = 1.0 / length * pricePerMass;

        return pricePerLength;
    }

}
