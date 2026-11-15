package com.example.blades360

import android.graphics.Bitmap

/** RAM-only ring of recent serialized states. Never writes or changes ROM files. */
class RewindHistory(private val budget:Long=96L*1024*1024,private val maxEntries:Int=90){
    data class Entry(val timeMs:Long,val state:ByteArray,var preview:Bitmap?=null)
    val entries=ArrayList<Entry>()
    var bytes=0L;private set
    fun add(time:Long,state:ByteArray):Entry?{if(state.isEmpty()||state.size>32*1024*1024||state.size>budget)return null;val result=Entry(time,state);entries.add(result);bytes+=state.size;while(entries.size>maxEntries||bytes>budget){remove(0)};return result}
    private fun remove(i:Int){val e=entries.removeAt(i);bytes-=e.state.size;e.preview?.recycle()}
    fun discardAfter(entry:Entry){val i=entries.indexOf(entry);if(i<0)return;while(entries.lastIndex>i)remove(entries.lastIndex)}
    fun clear(){while(entries.isNotEmpty())remove(entries.lastIndex)}
    companion object {fun format(ms:Long):String{val total=ms.coerceAtLeast(0)/1000;return "%d:%02d".format(total/60,total%60)}}
}
