package org.apache.lucene.analysis.da;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.tartarus.snowball.ext.DanishStemmer;

import java.util.Map;

public class DanishSnowballStemTokenFilterFactory extends TokenFilterFactory {

    /** SPI name */
    public static final String NAME = "danishSnowballStem";

    /** Creates a new DanishSnowballStemTokenFilterFactory */
    public DanishSnowballStemTokenFilterFactory(Map<String,String> args) {
        super(args);
        if (!args.isEmpty()) {
            throw new IllegalArgumentException("Unknown parameters: " + args);
        }
    }

    @Override
    public TokenStream create(TokenStream input) {
        return new SnowballFilter(input, new DanishStemmer());
    }
}
