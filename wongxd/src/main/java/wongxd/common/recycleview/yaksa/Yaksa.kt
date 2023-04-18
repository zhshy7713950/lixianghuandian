package wongxd.common.recycleview.yaksa

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import club.wongxd.mvvmrigger.base.kotin.recycleview.yaksa.YaksaAdapter
import club.wongxd.mvvmrigger.base.kotin.recycleview.yaksa.YaksaDsl


import wongxd.common.recycleview.flow.SpaceItemDecoration
import wongxd.common.recycleview.flow.SpecLayoutManager

const val LINEAR_LAYOUT = 0
const val GRID_LAYOUT = 1
const val STAGGERED_LAYOUT = 2
const val FLOW_LAYOUT = 3

/**
 * This function is used to create a Linear list.
 *
 *@param clear If true, all old items will be cleared and new items will be re-created.
 *             Otherwise, it will continue to add new items to the original data
 *
 *@param block Item dsl
 */
fun RecyclerView.linear(clear: Boolean = true, block: YaksaDsl.() -> Unit) {
    initDsl(this, clear, LINEAR_LAYOUT, block)
}

/**
 * This function is used to create a Grid list.
 *
 *@param clear If true, all old items will be cleared and new items will be re-created.
 *             Otherwise, it will continue to add new items to the original data
 *
 *@param block Item dsl
 */
fun RecyclerView.grid(clear: Boolean = true, block: YaksaDsl.() -> Unit) {
    initDsl(this, clear, GRID_LAYOUT, block)
}

/**
 * This function is used to create a Stagger list.
 *
 *@param clear If true, all old items will be cleared and new items will be re-created.
 *             Otherwise, it will continue to add new items to the original data
 *
 *@param block Item dsl
 */
fun RecyclerView.stagger(clear: Boolean = true, block: YaksaDsl.() -> Unit) {
    initDsl(this, clear, STAGGERED_LAYOUT, block)
}


/**
 * SpaceItemDecoration's spaceRow dp value
 */
private var spaceRow = 5


/**
 *
 *SpaceItemDecoration's spaceClo dp value
 */
private var spaceClo = 5

/**
 * This function is used to create a flow list.
 *
 *@param clear If true, all old items will be cleared and new items will be re-created.
 *             Otherwise, it will continue to add new items to the original data
 *
 *@param spaceRow SpaceItemDecoration's spaceRow dp value
 *
 *@param spaceClo SpaceItemDecoration's spaceClo dp value
 *
 *@param block Item dsl
 */
fun RecyclerView.flow(clear: Boolean = true, spaceRow: Int = 5, spaceClo: Int = 5, block: YaksaDsl .() -> Unit) {
    initDsl(this, clear, FLOW_LAYOUT, block)
}

private fun initDsl(target: RecyclerView, clear: Boolean, type: Int, block: YaksaDsl.() -> Unit) {
    checkAdapter(target)

    val adapter = target.adapter as YaksaAdapter

    val dsl = if (clear) {
        YaksaDsl(mutableListOf())
    } else {
        YaksaDsl(adapter.data)
    }

    dsl.block()

    initLayoutManager(target, dsl, type)
    adapter.submitList(dsl.dataSet)
}

private fun checkAdapter(target: RecyclerView) {
    if (target.adapter == null) {
        target.adapter = YaksaAdapter()
    }

    if (target.adapter !is YaksaAdapter) {
        throw IllegalStateException("Adapter must be YaksaAdapter")
    }
}

private fun initLayoutManager(target: RecyclerView, dsl: YaksaDsl, type: Int) {
    var needNew = true
    val source = target.layoutManager
    if (source != null) {
        needNew = checkNeedNew(source, dsl)
    }

    if (needNew) {
        val layoutManager = newLayoutManager(type, target, dsl)
        configureLayoutManager(layoutManager, dsl)
        target.layoutManager = layoutManager
    }
}

private fun newLayoutManager(type: Int, target: RecyclerView, dsl: YaksaDsl): LayoutManager {

    while (true) {
        if (target.itemDecorationCount > 0)
            target.removeItemDecorationAt(0)
        else
            break
    }

    return when (type) {
        LINEAR_LAYOUT -> LinearLayoutManager(target.context, dsl.orientation, dsl.reverse)
        GRID_LAYOUT -> GridLayoutManager(target.context, dsl.spanCount, dsl.orientation, dsl.reverse)
        STAGGERED_LAYOUT -> StaggeredGridLayoutManager(dsl.spanCount, dsl.orientation)
        FLOW_LAYOUT -> {
            target.addItemDecoration(SpaceItemDecoration(target.context, spaceRow, spaceClo))
            SpecLayoutManager()
        }
        else -> throw IllegalStateException("This should never happen!")
    }
}

private fun configureLayoutManager(layoutManager: LayoutManager, dsl: YaksaDsl) {
    if (layoutManager is GridLayoutManager) {
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return dsl.dataSet[position].gridSpanSize()
            }
        }
    }
}

private fun checkNeedNew(source: LayoutManager, dsl: YaksaDsl): Boolean {
    return when (source) {
        is GridLayoutManager -> dsl.checkGrid(source)            //Grid must check before Linear
        is LinearLayoutManager -> dsl.checkLinear(source)
        is StaggeredGridLayoutManager -> dsl.checkStagger(source)
        is SpecLayoutManager -> true
        else -> throw  IllegalStateException("This should never happen!")
    }
}


