package org.apache.lucene.monitor;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;

import java.io.IOException;
import java.util.*;

public class MyHighlightsMatch extends QueryMatch {

    public static final MatcherFactory<MyHighlightsMatch> MATCHER = searcher -> new CandidateMatcher<MyHighlightsMatch>(searcher) {

        @Override
        protected void matchQuery(String queryId, Query matchQuery, Map<String, String> metadata) throws IOException {
            Weight w = searcher.createWeight(searcher.rewrite(matchQuery), ScoreMode.COMPLETE, 1);
            for (LeafReaderContext ctx : searcher.getIndexReader().leaves()) {
                for (int i = 0; i < ctx.reader().maxDoc(); i++) {
                    Matches matches = w.matches(ctx, i);
                    // SCORE of the match
                    float score = w.scorer(ctx).score();
                    if (matches != null) {
                        addMatch(buildMatch(matches, queryId, score, i), i);
                    }
                }
            }
        }

        @Override
        public MyHighlightsMatch resolve(MyHighlightsMatch match1, MyHighlightsMatch match2) {
            return MyHighlightsMatch.merge(match1.getQueryId(), match1, match2);
        }

        private MyHighlightsMatch buildMatch(Matches matches, String queryId, float score, int docId) throws IOException {
            MyHighlightsMatch m = new MyHighlightsMatch(queryId, score, docId);
            for (String field : matches) {
                MatchesIterator mi = matches.getMatches(field);

                while (mi.next()) {
                    MatchesIterator sub = mi.getSubMatches();
                    if (sub != null) {
                        while (sub.next()) {
                            m.addHit(field, sub.startPosition(), sub.endPosition(), sub.startOffset(), sub.endOffset());
                        }
                    } else {
                        m.addHit(field, mi.startPosition(), mi.endPosition(), mi.startOffset(), mi.endOffset());
                    }
                }
            }
            return m;
        }
    };

    private final Map<String, Set<MyHighlightsMatch.Hit>> hits;
    private float score;
    private final int docId;

    MyHighlightsMatch(String queryId, float score, int docId) {
        super(queryId);
        this.hits = new TreeMap<>();
        this.score = score;
        this.docId = docId;
    }

    /**
     * @return a map of hits per field
     */
    public Map<String, Set<MyHighlightsMatch.Hit>> getHits() {
        return Collections.unmodifiableMap(this.hits);
    }

    /**
     * @return the fields in which matches have been found
     */
    public Set<String> getFields() {
        return Collections.unmodifiableSet(hits.keySet());
    }


    public float getScore() {
        return this.score;
    }

    public int getDocId() {
        return docId;
    }

    private void setScore(float score) {
        this.score = score;
    }

    /**
     * Get the hits for a specific field
     *
     * @param field the field
     * @return the Hits found in this field
     */
    public Collection<MyHighlightsMatch.Hit> getHits(String field) {
        Collection<MyHighlightsMatch.Hit> found = hits.get(field);
        if (found != null)
            return Collections.unmodifiableCollection(found);
        return Collections.emptyList();
    }

    /**
     * @return the total number of hits for the query
     */
    public int getHitCount() {
        int c = 0;
        for (Set<MyHighlightsMatch.Hit> fieldhits : hits.values()) {
            c += fieldhits.size();
        }
        return c;
    }

    static MyHighlightsMatch merge(String queryId, MyHighlightsMatch... matches) {
        float summedScore = 0f;
        MyHighlightsMatch newMatch = new MyHighlightsMatch(queryId, 0, matches[0].docId);
        for (MyHighlightsMatch match : matches) {
            summedScore += match.score;
            for (String field : match.getFields()) {
                Set<MyHighlightsMatch.Hit> hitSet = newMatch.hits.computeIfAbsent(field, f -> new TreeSet<>());
                hitSet.addAll(match.getHits(field));
            }
        }
        newMatch.setScore(summedScore);
        return newMatch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MyHighlightsMatch)) return false;
        if (!super.equals(o)) return false;

        MyHighlightsMatch that = (MyHighlightsMatch) o;

        if (hits != null ? !hits.equals(that.hits) : that.hits != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (hits != null ? hits.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return super.toString() + "{hits=" + hits + "}";
    }

    void addHit(String field, int startPos, int endPos, int startOffset, int endOffset) {
        Set<MyHighlightsMatch.Hit> hitSet = hits.computeIfAbsent(field, f -> new TreeSet<>());
        hitSet.add(new MyHighlightsMatch.Hit(startPos, startOffset, endPos, endOffset));
    }

    /**
     * Represents an individual hit
     */
    public static class Hit implements Comparable<MyHighlightsMatch.Hit> {

        /**
         * The start position
         */
        public final int startPosition;

        /**
         * The start offset
         */
        public final int startOffset;

        /**
         * The end positions
         */
        public final int endPosition;

        /**
         * The end offset
         */
        public final int endOffset;

        public Hit(int startPosition, int startOffset, int endPosition, int endOffset) {
            this.startPosition = startPosition;
            this.startOffset = startOffset;
            this.endPosition = endPosition;
            this.endOffset = endOffset;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof MyHighlightsMatch.Hit))
                return false;
            MyHighlightsMatch.Hit other = (MyHighlightsMatch.Hit) obj;
            return this.startOffset == other.startOffset &&
                    this.endOffset == other.endOffset &&
                    this.startPosition == other.startPosition &&
                    this.endPosition == other.endPosition;
        }

        @Override
        public int hashCode() {
            int result = startPosition;
            result = 31 * result + startOffset;
            result = 31 * result + endPosition;
            result = 31 * result + endOffset;
            return result;
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "%d(%d)->%d(%d)", startPosition, startOffset, endPosition, endOffset);
        }

        @Override
        public int compareTo(MyHighlightsMatch.Hit other) {
            if (this.startPosition != other.startPosition)
                return Integer.compare(this.startPosition, other.startPosition);
            return Integer.compare(this.endPosition, other.endPosition);
        }
    }
}
