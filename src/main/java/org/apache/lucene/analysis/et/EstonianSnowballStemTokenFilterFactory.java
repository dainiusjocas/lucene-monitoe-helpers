package org.apache.lucene.analysis.et;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.tartarus.snowball.ext.EstonianStemmer;

import java.util.Map;

public class EstonianSnowballStemTokenFilterFactory extends TokenFilterFactory {

    /** SPI name */
    public static final String NAME = "basqueSnowballStem";

    /** Creates a new EstonianSnowballStemTokenFilterFactory */
    public EstonianSnowballStemTokenFilterFactory(Map<String,String> args) {
        super(args);
        if (!args.isEmpty()) {
            throw new IllegalArgumentException("Unknown parameters: " + args);
        }
    }

    @Override
    public TokenStream create(TokenStream input) {
        return new SnowballFilter(input, new EstonianStemmer());
    }
}
