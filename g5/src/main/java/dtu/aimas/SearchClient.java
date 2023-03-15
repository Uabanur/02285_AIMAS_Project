package dtu.aimas;

import dtu.aimas.communication.IO;
import dtu.aimas.communication.LogLevel;
import dtu.aimas.config.Configuration;
import dtu.aimas.parsers.ArgumentParser;
import dtu.aimas.parsers.CourseLevelParser;
import dtu.aimas.search.Problem;

public class SearchClient 
{
    public static Configuration config;
    public static void main(String[] args) throws Throwable
    {
        IO.logLevel = LogLevel.Debug;
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

    private static void handleConfigs(String[] args) throws Throwable {
        var configuration = ArgumentParser.parse(args);
        
        IO.debug(configuration);
        if(configuration.isError()){
            IO.error("Invalid arguments.");
            IO.logException(configuration.getError());
            throw configuration.getError();
        }

        SearchClient.config = configuration.get();
    }

    private static Problem logStart(Problem p){
        IO.debug("Problem:\n%s", p.toString()); 
        return p;
    }
}
