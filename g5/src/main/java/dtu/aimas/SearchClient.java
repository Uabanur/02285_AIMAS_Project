package dtu.aimas;

import java.io.IOException;

import dtu.aimas.communication.IO;
import dtu.aimas.communication.LogLevel;
import dtu.aimas.parsers.ArgumentParser;
import dtu.aimas.parsers.CourseLevelParser;
import dtu.aimas.search.Problem;

public class SearchClient 
{
    public static void main(String[] args) throws IOException
    {
        IO.logLevel = LogLevel.Debug;

        var result = IO.initializeServerCommunication(CourseLevelParser.Instance)
            .map(SearchClient::logStart)
            .flatMap(p -> 
                ArgumentParser.parseSolverFromArguments(args)
                    .flatMap(s -> s.solve(p)))
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

    private static Problem logStart(Problem p){
        IO.debug("Problem:\n%s", p.toString()); 
        return p;
    }
}
