package com.thedailyflare.quora

import android.app.*
import android.os.*
import android.content.*
import android.graphics.Color
import android.view.*
import android.widget.*
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity: Activity() {
 private val feed="https://thedailyflare.com/feed/"
 private val posted by lazy { getSharedPreferences("posted",0) }
 override fun onCreate(b:Bundle?){super.onCreate(b); load()}
 private fun load(){ val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(28,28,28,20)}; val bar=LinearLayout(this); val title=TextView(this).apply{text="THE DAILY FLARE";textSize=24f;setTextColor(Color.DKGRAY)}; val refresh=Button(this).apply{text="Refresh";setOnClickListener{load()}};bar.addView(title,LinearLayout.LayoutParams(0,-2,1f));bar.addView(refresh);root.addView(bar); val status=TextView(this).apply{text="Loading latest articles..."};root.addView(status);val scroll=ScrollView(this);val list=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};scroll.addView(list);root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f));setContentView(root);Thread{try{val d=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(URL(feed).openStream());val items=d.getElementsByTagName("item");runOnUiThread{status.text="Latest articles";for(i in 0 until items.length.coerceAtMost(30)){val n=items.item(i) as org.w3c.dom.Element;val t=n.getElementsByTagName("title").item(0)?.textContent?:continue;val l=n.getElementsByTagName("link").item(0)?.textContent?:"";val desc=n.getElementsByTagName("description").item(0)?.textContent?:"";addCard(list,t,l,desc)}}}catch(e:Exception){runOnUiThread{status.text="Could not load RSS. Tap Refresh."}}}.start() }
 private fun addCard(list:LinearLayout,t:String,l:String,d:String){val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(18,18,18,18)};val tv=TextView(this).apply{text=t;textSize=18f;setTextColor(Color.DKGRAY)};val ex=d.replace(Regex("<[^>]*>"),"").replace(Regex("\\s+")," ").trim().take(500);val preview=TextView(this).apply{text=ex;textSize=14f};val row=LinearLayout(this);val copy=Button(this).apply{text="Copy Post";setOnClickListener{val text="$t\\n\\n$ex\\n\\nRead the full story:\\n$l";(getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager).setPrimaryClip(android.content.ClipData.newPlainText("Quora Post",text));Toast.makeText(this@MainActivity,"Copied",Toast.LENGTH_SHORT).show()}};val open=Button(this).apply{text="Open Quora";setOnClickListener{startActivity(Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://www.quora.com/")))}};val mark=Button(this).apply{text=if(posted.getBoolean(l,false))"Posted ✓" else "Mark Posted";setOnClickListener{posted.edit().putBoolean(l,true).apply();text="Posted ✓"}};row.addView(copy);row.addView(open);row.addView(mark);box.addView(tv);box.addView(preview);box.addView(row);list.addView(box);val line=View(this).apply{setBackgroundColor(Color.LTGRAY)};list.addView(line,LinearLayout.LayoutParams(-1,1)) }
}
