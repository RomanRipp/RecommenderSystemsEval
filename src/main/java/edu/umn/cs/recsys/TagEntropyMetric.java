package edu.umn.cs.recsys;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import edu.umn.cs.recsys.dao.ItemTagDAO;
import edu.umn.cs.recsys.dao.TagFile;

import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.eval.algorithm.AlgorithmInstance;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.AbstractTestUserMetric;
import org.grouplens.lenskit.eval.metrics.TestUserMetricAccumulator;
import org.grouplens.lenskit.eval.metrics.topn.ItemSelectors;
import org.grouplens.lenskit.eval.traintest.TestUser;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A metric that measures the tag entropy of the recommended items.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TagEntropyMetric extends AbstractTestUserMetric {
    private final int listSize;
    private final List<String> columns;
    private final Map<Long, Set<String>> cache;

    /**
     * Construct a new tag entropy metric.
     * 
     * @param nitems The number of items to request.
     */
    public TagEntropyMetric(int nitems) {
        listSize = nitems;
        // initialize column labels with list length
        columns = ImmutableList.of(String.format("TagEntropy@%d", nitems));
        cache = new ConcurrentHashMap<Long, Set<String>>();
    }

    /**
     * Make a metric accumulator.  Metrics operate with <em>accumulators</em>, which are created
     * for each algorithm and data set.  The accumulator measures each user's output, and
     * accumulates the results into a global statistic for the whole evaluation.
     *
     * @param algorithm The algorithm being tested.
     * @param data The data set being tested with.
     * @return An accumulator for analyzing this algorithm and data set.
     */
    @Override
    public TestUserMetricAccumulator makeAccumulator(AlgorithmInstance algorithm, TTDataSet data) {
        return new TagEntropyAccumulator();
    }

    /**
     * Return the labels for the (global) columns returned by this metric.
     * @return The labels for the global columns.
     */
    @Override
    public List<String> getColumnLabels() {
        return columns;
    }

    /**
     * Return the lables for the per-user columns returned by this metric.
     */
    @Override
    public List<String> getUserColumnLabels() {
        // per-user and global have the same fields, they just differ in aggregation.
        return columns;
    }


    private class TagEntropyAccumulator implements TestUserMetricAccumulator {
        private double totalEntropy = 0;
        private int userCount = 0;

        /**
         * Evaluate a single test user's recommendations or predictions.
         * @param testUser The user's recommendation result.
         * @return The values for the per-user columns.
         */
        @Nonnull
        @Override
        public Object[] evaluate(TestUser testUser) {
            List<ScoredId> recommendations =
                    testUser.getRecommendations(listSize,
                                                ItemSelectors.allItems(),
                                                ItemSelectors.trainingItems());
            if (recommendations == null) {
                return new Object[1];
            }
            LenskitRecommender lkrec = (LenskitRecommender) testUser.getRecommender();
            ItemTagDAO tagDAO = lkrec.get(ItemTagDAO.class);
            TagVocabulary vocab = lkrec.get(TagVocabulary.class);

            double entropy = 0.0;          
            // TODO Implement the entropy metric
            for (String tag : vocab.getTagsList()){
            	double Pt = 0.0;
            	for (ScoredId recommendation : recommendations){
            		long item = recommendation.getId();
            		Set<String> movieTags = getMovieTags(item, tagDAO);
            		if (movieTags.contains(tag.toLowerCase())){
            			Pt += 1.0 / movieTags.size();
            		}
            	}
            	int size = recommendations.size();
            	if (Pt != 0.0 && size != 0){
            		Pt /= size;
            		
            		entropy -= Pt * Math.log(Pt);
            	}
            }
            entropy /= Math.log(2.0);
            totalEntropy += entropy;
            userCount += 1;
            System.out.println("The average entropy: " + totalEntropy / userCount
            		+" Total entropy: "+totalEntropy
            		+" User counnt: "+userCount
            		+" Entropy: "+entropy
            		);
            return new Object[]{entropy};
        } 

        private Set<String> getMovieTags(long item, ItemTagDAO tagDAO) {
			if (cache.containsKey(item)) {
				return cache.get(item);
			} else {
				Set<String> tagVect = normalizeTags(tagDAO.getItemTags(item));
				cache.put(item, tagVect);
				return tagVect;
			}
		}

		/**
         * generate the list of movie tags that contains only unique lower case tags
         * @param L
         * @return
         */
        private Set<String> normalizeTags(List<String> L){
        	Set<String>  lowerCaseL = new HashSet<String>();
        	for(String s : L){
        			lowerCaseL.add(s.toLowerCase());
        	}
        	return lowerCaseL;
        }
        
        /**
         * Get the final aggregate results.  This is called after all users have been evaluated, and
         * returns the values for the columns in the global output.
         *
         * @return The final, aggregated columns.
         */
        @Nonnull
        @Override
        public Object[] finalResults() {
            // return a single field, the average entropy
        	System.out.println("The average entropy: " + totalEntropy / userCount);
            return new Object[]{totalEntropy / userCount};
        }
    }
}
