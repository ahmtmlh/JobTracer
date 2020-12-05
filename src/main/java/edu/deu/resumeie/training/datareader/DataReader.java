package edu.deu.resumeie.training.datareader;

import edu.deu.resumeie.training.nlp.informationextraction.InformationExtractor;

import java.io.IOException;
import java.util.List;

public interface DataReader {

    void readRows(int n);
    void close() throws IOException;
    List<InformationExtractor.ListItem> exportAsList();

}
