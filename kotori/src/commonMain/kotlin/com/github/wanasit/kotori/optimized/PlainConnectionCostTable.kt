package com.github.wanasit.kotori.optimized

import com.github.wanasit.kotori.ConnectionCost
import com.github.wanasit.kotori.TermEntry
import com.github.wanasit.kotori.utils.IOUtils
import okio.Sink
import okio.Source

class PlainConnectionCostTable(
        val fromIdCardinality: Int,
        val toIdCardinality: Int,
        private val table: ShortArray = ShortArray(fromIdCardinality * toIdCardinality) { 0 }
) : ConnectionCost {

    override fun lookup(fromRightId: Int, toLeftId: Int): Int {
        return this.table[index(fromRightId, toLeftId)].toInt()
    }

    fun put(fromId: Int, toId: Int, cost: Int) {
        if (cost<Short.MIN_VALUE || cost > Short.MAX_VALUE) {
            throw UnsupportedOperationException()
        }

        table[index(fromId, toId)] = cost.toShort()
    }

    private fun index(fromId: Int, toId: Int) : Int {
        return fromIdCardinality * toId + fromId
    }

    companion object {

        fun copyOf(entries: Iterable<TermEntry<*>>, connectionCost: ConnectionCost) : PlainConnectionCostTable {
            val maxLeftId = entries.map { it.leftId }.max()!!
            val maxRightId = entries.map { it.rightId }.max()!!
            return copyOf(maxRightId + 1, maxLeftId + 1) { leftId, rightId ->
                connectionCost.lookup(leftId, rightId)
            }
        }

        fun copyOf(fromIdCardinality: Int, toIdCardinality: Int, connectionCost: (leftId:Int, rightId:Int) -> Int) : PlainConnectionCostTable {
            val array = PlainConnectionCostTable(fromIdCardinality, toIdCardinality)
            for (i in 0 until fromIdCardinality) {
                for (j in 0 until toIdCardinality) {
                    array.put(i, j, connectionCost(i, j))
                }
            }

            return array
        }

        fun readFromInputStream(inputStream: Source) : PlainConnectionCostTable {
            return PlainConnectionCostTable(
                    IOUtils.readInt(inputStream), IOUtils.readInt(inputStream), IOUtils.readShortArray(inputStream))
        }

        fun writeToOutputStream(outputStream: Sink, value: PlainConnectionCostTable) {
            IOUtils.writeInt(outputStream, value.fromIdCardinality)
            IOUtils.writeInt(outputStream, value.toIdCardinality)
            IOUtils.writeShortArray(outputStream, value.table)
        }
    }
}