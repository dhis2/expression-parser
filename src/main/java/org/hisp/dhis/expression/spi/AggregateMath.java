package org.hisp.dhis.expression.spi;

import java.util.Arrays;
import java.util.stream.DoubleStream;

/**
 * Math operations for aggregation functions.
 *
 * Some computations were extracted and simplified from apache math.
 *
 * @author Jan Bernitt
 */
final class AggregateMath {

    private AggregateMath() {
        throw new UnsupportedOperationException("util");
    }

    static double avg(double[] values) {
        double sampleSize = values.length;
        double xbar = sum(values) / sampleSize;
        double correction = 0.0d;

        for (double value : values) {
            correction += value - xbar;
        }

        return xbar + correction / sampleSize;
    }

    static double count(double[] values) {
        return values.length;
    }

    /**
     * Vector maximum.
     *
     * @param values input vector
     * @return largest of the values ignoring NaNs or NaN if values has length zero
     */
    static double max(double[] values) {
        return DoubleStream.of(values).filter(v -> !Double.isNaN(v)).max().orElse(Double.NaN);
    }

    static double median(double[] values) {
        return 0d;
    }

    /**
     * Vector minimum.
     *
     * @param values input vector
     * @return smallest of the values ignoring NaNs or NaN if values has length zero
     */

    static double min(double[] values) {
        return DoubleStream.of(values).filter(v -> !Double.isNaN(v)).min().orElse(Double.NaN);
    }

    /**
     * Vector function: percentileCont (continuous percentile)
     * <p/>
     * The percentileCont function is equivalent to the PostgreSQL function
     * percentile_cont and the Excel function PERCENTILE.INC
     *
     * @author Jim Grace
     */
    static Double percentileCont(double[] values, Number fraction) {
        if (values.length == 0 || fraction == null || fraction.doubleValue() < 0d || fraction.doubleValue() > 1d )
        {
            return null;
        }
        Arrays.sort( values );
        if ( fraction.doubleValue() == 0d )
        {
            return values[0];
        }
        double[] work = DoubleStream.of(values).filter(v -> !Double.isNaN(v)).toArray();
        if (work.length == 0) {
            return Double.NaN; // TODO make null to be consistent?
        }
        int[] pivotsHeap = new int[512];
        Arrays.fill(pivotsHeap, -1);
        double pos = indexR7(fraction.doubleValue(), work.length);
        return estimate(work, pivotsHeap, pos);
    }

    static double stddev(double[] values) {
        return 0d;
    }

    static double stddevPop(double[] values) {
        return 0d;
    }

    static double stddevSamp(double[] values) {
        return 0d;
    }

    /**
     * Vector sum.
     *
     * @param values input vector
     * @return sum of all values
     */
    static double sum(double[] values) {
        return DoubleStream.of(values).sum();
    }

    static double variance(double[] values) {
        return 0d;
    }

    private static double estimate(double[] work, int[] pivotsHeap, double pos) {
        int length = work.length;
        double fpos = Math.floor(pos);
        int intPos = (int)fpos;
        double dif = pos - fpos;
        if (pos < 1.0) {
            return KthSelector.select(work, pivotsHeap, 0);
        } else if (pos >= length) {
            return KthSelector.select(work, pivotsHeap, length - 1);
        } else {
            double lower = KthSelector.select(work, pivotsHeap, intPos - 1);
            double upper = KthSelector.select(work, pivotsHeap, intPos);
            return lower + dif * (upper - lower);
        }
    }

    private static double indexR7(double p, int length) {
        return Double.compare(p, 0.0) == 0 ? 0.0 : (Double.compare(p, 1.0) == 0 ? (double)length : 1.0 + (length - 1) * p);
    }

    /**
     * Extracted from {@code org.apache.commons.math3.util.KthSelector} and
     * {@code org.apache.commons.math3.util.MedianOf3PivotingStrategy}.
     */
    static class KthSelector {

        static double select(double[] work, int[] pivotsHeap, int k) {
            int begin = 0;
            int end = work.length;
            int node = 0;
            boolean usePivotsHeap = pivotsHeap != null;

            while (end - begin > 15) {
                int pivot;
                if (usePivotsHeap && node < pivotsHeap.length && pivotsHeap[node] >= 0) {
                    pivot = pivotsHeap[node];
                } else {
                    pivot = partition(work, begin, end, pivotIndex(work, begin, end));
                    if (usePivotsHeap && node < pivotsHeap.length) {
                        pivotsHeap[node] = pivot;
                    }
                }

                if (k == pivot) {
                    return work[k];
                }

                if (k < pivot) {
                    end = pivot;
                    node = Math.min(2 * node + 1, usePivotsHeap ? pivotsHeap.length : pivot);
                } else {
                    begin = pivot + 1;
                    node = Math.min(2 * node + 2, usePivotsHeap ? pivotsHeap.length : end);
                }
            }

            Arrays.sort(work, begin, end);
            return work[k];
        }

        private static int partition(double[] work, int begin, int end, int pivot) {
            double value = work[pivot];
            work[pivot] = work[begin];
            int i = begin + 1;
            int j = end - 1;

            while (i < j) {
                while (i < j && work[j] > value) {
                    --j;
                }

                while (i < j && work[i] < value) {
                    ++i;
                }

                if (i < j) {
                    double tmp = work[i];
                    work[i++] = work[j];
                    work[j--] = tmp;
                }
            }

            if (i >= end || work[i] > value) {
                --i;
            }

            work[begin] = work[i];
            work[i] = value;
            return i;
        }

        static int pivotIndex(double[] work, int begin, int end) {
            int inclusiveEnd = end - 1;
            int middle = begin + (inclusiveEnd - begin) / 2;
            double wBegin = work[begin];
            double wMiddle = work[middle];
            double wEnd = work[inclusiveEnd];
            if (wBegin < wMiddle) {
                if (wMiddle < wEnd) {
                    return middle;
                } else {
                    return wBegin < wEnd ? inclusiveEnd : begin;
                }
            } else if (wBegin < wEnd) {
                return begin;
            } else {
                return wMiddle < wEnd ? inclusiveEnd : middle;
            }
        }
    }
}
