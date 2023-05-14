package dtu.aimas;

import dtu.aimas.communication.IO;
import dtu.aimas.config.Configuration;
import dtu.aimas.parsers.ArgumentParser;
import dtu.aimas.parsers.CourseLevelParser;
import dtu.aimas.search.Problem;

import java.util.Arrays;

public class SearchClient 
{
    public static Configuration config;
    public static void main(String[] args)
    {
        IO.useServerCommunication();
        handleConfigs(args);
        
        var result = IO.initializeServerCommunication(CourseLevelParser.Instance)
            .map(SearchClient::logStart)
            .flatMap(p -> SearchClient.config.getSolver().solve(p))
            .flatMap(IO::sendSolutionToServer);

        if(result.isOk())
        {
            IO.info("Solution found");
        }
        else
        {
            IO.info("Error occured");
            IO.logException(result.getError());
        }
    }

    private static void handleConfigs(String[] args) {
        var configuration = ArgumentParser.parse(args);

        if(configuration.isError()){
            IO.error("Invalid arguments.");
            IO.error(args);
            IO.logException(configuration.getError());
            System.exit(0);
        }

        SearchClient.config = configuration.get();
        config.configureIO();

        IO.debug("arguments: " + Arrays.toString(args));
        IO.debug(configuration);
    }

    private static Problem logStart(Problem p){
        IO.debug("Problem:\n%s", p.toString()); 
        return p;
    }
}
