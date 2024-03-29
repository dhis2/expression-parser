package org.hisp.dhis.lib.expression.math

import kotlin.math.floor

/**
 * Math operations for aggregation functions.
 *
 *
 * Some computations were extracted and simplified from apache math.
 *
 * @author Jan Bernitt
 */
object VectorAggregation {

    fun avg(values: DoubleArray): Double {
        val sampleSize = values.size.toDouble()
        val xbar = sum(values) / sampleSize
        var correction = 0.0
        for (value in values) {
            correction += value - xbar
        }
        return xbar + correction / sampleSize
    }

    fun count(values: DoubleArray): Double {
        return values.size.toDouble()
    }

    /**
     * Vector maximum.
     *
     * @param values input vector
     * @return largest of the values ignoring NaNs or NaN if values has length zero
     */
    fun max(values: DoubleArray): Double {
        return values.filter { v: Double -> !v.isNaN() }.maxOrNull() ?: Double.NaN
    }

    fun mean(values: DoubleArray): Double {
        return values.sum() / values.size
    }

    fun median(values: DoubleArray): Double? {
        return percentileCont(values, 0.5)
    }

    /**
     * Vector minimum.
     *
     * @param values input vector
     * @return smallest of the values ignoring NaNs or NaN if values has length zero
     */
    fun min(values: DoubleArray): Double {
        return values.filter { v: Double -> !v.isNaN() }.minOrNull() ?: Double.NaN
    }

    /**
     * Vector function: percentileCont (continuous percentile)
     *
     * The percentileCont function is equivalent to the PostgreSQL function percentile_cont and the Excel function
     * PERCENTILE.INC
     *
     * @author Jim Grace
     */
    fun percentileCont(values: DoubleArray, fraction: Number?): Double? {
        if (values.isEmpty() || fraction == null || fraction.toDouble() < 0.0 || fraction.toDouble() > 1.0) {
            return null
        }
        values.sort()
        if (fraction.toDouble() == 0.0) {
            return values[0]
        }
        val work = values.filter { v: Double -> !v.isNaN() } .toDoubleArray()
        if (work.isEmpty()) {
            return Double.NaN // TODO make null to be consistent?
        }
        val pivotsHeap = IntArray(512)
        pivotsHeap.fill( -1)
        val pos = indexR7(fraction.toDouble(), work.size)
        return estimate(work, pivotsHeap, pos)
    }

    fun stddevPop(values: DoubleArray): Double {
        return kotlin.math.sqrt(variance(values, false))
    }

    fun stddevSamp(values: DoubleArray): Double {
        return kotlin.math.sqrt(variance(values))
    }

    /**
     * Vector sum.
     *
     * @param values input vector
     * @return sum of all values
     */
    fun sum(values: DoubleArray): Double {
        return values.sum()
    }

    fun variance(values: DoubleArray, biasCorrected: Boolean = true): Double {
        val mean = mean(values)
        val meanDelta = values.map { x -> x - mean }
        val meanDeltaSquared = meanDelta.map { x -> x * x }
        val correction = if (biasCorrected) 1 else 0
        return meanDeltaSquared.sum() / (values.size - correction)
    }

    private fun estimate(work: DoubleArray, pivotsHeap: IntArray, pos: Double): Double {
        val length = work.size
        val fpos = floor(pos)
        val intPos = fpos.toInt()
        val dif = pos - fpos
        return if (pos < 1.0) {
            KthSelector.select(work, pivotsHeap, 0)
        }
        else if (pos >= length) {
            KthSelector.select(work, pivotsHeap, length - 1)
        }
        else {
            val lower = KthSelector.select(work, pivotsHeap, intPos - 1)
            val upper = KthSelector.select(work, pivotsHeap, intPos)
            lower + dif * (upper - lower)
        }
    }

    private fun indexR7(p: Double, length: Int): Double {
        return when (p) {
            0.0 -> 0.0
            1.0 -> length.toDouble()
            else -> 1.0 + (length - 1) * p
        }
    }

    /**
     * Extracted from `org.apache.commons.math3.util.KthSelector` and
     * `org.apache.commons.math3.util.MedianOf3PivotingStrategy`.
     */
    internal object KthSelector {
        fun select(work: DoubleArray, pivotsHeap: IntArray, k: Int): Double {
            var begin = 0
            var end = work.size
            var node = 0
            val usePivotsHeap = true
            while (end - begin > 15) {
                var pivot: Int
                if (usePivotsHeap && node < pivotsHeap.size && pivotsHeap[node] >= 0) {
                    pivot = pivotsHeap[node]
                }
                else {
                    pivot = partition(work, begin, end, pivotIndex(work, begin, end))
                    if (usePivotsHeap && node < pivotsHeap.size) {
                        pivotsHeap[node] = pivot
                    }
                }
                if (k == pivot) {
                    return work[k]
                }
                if (k < pivot) {
                    end = pivot
                    node = (2 * node + 1).coerceAtMost(if (usePivotsHeap) pivotsHeap.size else pivot)
                }
                else {
                    begin = pivot + 1
                    node = (2 * node + 2).coerceAtMost(if (usePivotsHeap) pivotsHeap.size else end)
                }
            }
            work.sort(begin, end)
            return work[k]
        }

        private fun partition(work: DoubleArray, begin: Int, end: Int, pivot: Int): Int {
            val value = work[pivot]
            work[pivot] = work[begin]
            var i = begin + 1
            var j = end - 1
            while (i < j) {
                while (i < j && work[j] > value) {
                    --j
                }
                while (i < j && work[i] < value) {
                    ++i
                }
                if (i < j) {
                    val tmp = work[i]
                    work[i++] = work[j]
                    work[j--] = tmp
                }
            }
            if (i >= end || work[i] > value) {
                --i
            }
            work[begin] = work[i]
            work[i] = value
            return i
        }

        private fun pivotIndex(work: DoubleArray, begin: Int, end: Int): Int {
            val inclusiveEnd = end - 1
            val middle = begin + (inclusiveEnd - begin) / 2
            val wBegin = work[begin]
            val wMiddle = work[middle]
            val wEnd = work[inclusiveEnd]
            return if (wBegin < wMiddle) {
                if (wMiddle < wEnd) {
                    middle
                }
                else {
                    if (wBegin < wEnd) inclusiveEnd else begin
                }
            }
            else if (wBegin < wEnd) {
                begin
            }
            else {
                if (wMiddle < wEnd) inclusiveEnd else middle
            }
        }
    }
}
