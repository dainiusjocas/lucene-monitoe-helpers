package org.apache.lucene.monitor;

import org.apache.lucene.search.Scorable;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;

import java.io.IOException;

public class MyScoringMatch extends QueryMatch {

    public static final MatcherFactory<MyScoringMatch> matchWithSimilarity(Similarity similarity) {
        return searcher -> {
            searcher.setSimilarity(similarity);
            return new CollectingMatcher<MyScoringMatch>(searcher, ScoreMode.COMPLETE) {
                @Override
                protected MyScoringMatch doMatch(String queryId, int doc, Scorable scorer) throws IOException {
//                    System.out.println(searcher.doc(doc));
//                    String[] vals = searcher.doc(doc).getValues("_id");
//                    System.out.println(Arrays.toString(vals));
                    float score = scorer.score();
                    if (score > 0)
                        return new MyScoringMatch(queryId, score, doc);
                    return null;
                }

                @Override
                public MyScoringMatch resolve(MyScoringMatch match1, MyScoringMatch match2) {
                    return new MyScoringMatch(match1.getQueryId(), match1.getScore() + match2.getScore(), match1.getDocId());
                }
            };
        };
    }

    public static final MatcherFactory<MyScoringMatch> DEFAULT_MATCHER = matchWithSimilarity(new BM25Similarity());

    private final float score;
    private final int docId;

    private MyScoringMatch(String queryId, float score, int docId) {
        super(queryId);
        this.score = score;
        this.docId = docId;
    }

    public float getScore() {
        return score;
    }

    public int getDocId() {
        return docId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MyScoringMatch)) return false;
        if (!super.equals(o)) return false;
        MyScoringMatch that = (MyScoringMatch) o;
        return Float.compare(that.score, score) == 0;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (score != +0.0f ? Float.floatToIntBits(score) : 0);
        return result;
    }
}
