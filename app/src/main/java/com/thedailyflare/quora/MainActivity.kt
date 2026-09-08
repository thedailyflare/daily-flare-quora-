package com.thedailyflare.quora

import android.app.Activity
import android.content.*
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity : Activity() {
 private val feed="https://thedailyflare.com/feed/"
 private val posted by lazy { getSharedPreferences("posted", MODE_PRIVATE) }
 private val navy=Color.rgb(23,42,58); private val ink=Color.rgb(32,38,43)
 private val muted=Color.rgb(105,113,120); private val cream=Color.rgb(247,246,243)
 private val gold=Color.rgb(201,168,106); private val green=Color.rgb(71,117,91)
 private lateinit var list:LinearLayout; private lateinit var status:TextView

 override fun onCreate(b:Bundle?){super.onCreate(b); render(); loadFeed()}
 private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
 private fun shape(c:Int,r:Int=0,s:Int=0,sc:Int=Color.TRANSPARENT)=GradientDrawable().apply{setColor(c);cornerRadius=dp(r).toFloat();if(s>0)setStroke(dp(s),sc)}
 private fun tv(v:String,z:Float,c:Int,b:Boolean=false)=TextView(this).apply{text=v;textSize=z;setTextColor(c);if(b)typeface=Typeface.create("sans-serif",Typeface.BOLD)}

 private fun render(){
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(cream)}
  val head=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(22),dp(22),dp(22),dp(18));background=shape(navy)}
  val top=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL}
  top.addView(tv("THE DAILY FLARE",21f,Color.WHITE,true).apply{letterSpacing=.08f},LinearLayout.LayoutParams(0,dp(48),1f))
  top.addView(tv("↻",28f,Color.WHITE,true).apply{gravity=Gravity.CENTER;background=shape(Color.argb(30,255,255,255),18);setOnClickListener{loadFeed()}},LinearLayout.LayoutParams(dp(52),dp(48)))
  head.addView(top);head.addView(tv("QUORA SHARING DESK",11f,gold,true).apply{letterSpacing=.12f})
  head.addView(tv("Turn today's stories into ready-to-share posts.",15f,Color.rgb(220,226,230)).apply{setPadding(0,dp(5),0,0)})
  root.addView(head)
  val body=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(18),dp(18),dp(18),dp(8))}
  val row=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL};status=tv("Loading latest stories…",18f,ink,true)
  row.addView(status,LinearLayout.LayoutParams(0,dp(40),1f));row.addView(tv("LIVE",10f,green,true).apply{gravity=Gravity.CENTER;background=shape(Color.rgb(228,239,232),14);setPadding(dp(10),dp(6),dp(10),dp(6))});body.addView(row)
  body.addView(tv("Choose a story, copy the prepared post, then share it on Quora.",13f,muted).apply{setPadding(0,0,0,dp(12))})
  val scroll=ScrollView(this);list=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};scroll.addView(list);body.addView(scroll,LinearLayout.LayoutParams(-1,0,1f));root.addView(body,LinearLayout.LayoutParams(-1,0,1f))
  root.addView(tv("THE DAILY FLARE  •  NEWS THAT MATTERS",10f,muted,true).apply{gravity=Gravity.CENTER;background=shape(Color.WHITE);setPadding(dp(18),dp(14),dp(18),dp(16));letterSpacing=.08f})
  setContentView(root)
 }

 private fun loadFeed(){
  status.text="Refreshing stories…";list.removeAllViews()
  Thread{try{
   val f=DocumentBuilderFactory.newInstance().apply{isNamespaceAware=false;setFeature("http://apache.org/xml/features/disallow-doctype-decl",true)}
   val doc=f.newDocumentBuilder().parse(URL(feed).openStream());val items=doc.getElementsByTagName("item");val stories=mutableListOf<Story>()
   for(i in 0 until items.length.coerceAtMost(30)){val n=items.item(i) as org.w3c.dom.Element;val title=n.getElementsByTagName("title").item(0)?.textContent?.trim().orEmpty();val link=n.getElementsByTagName("link").item(0)?.textContent?.trim().orEmpty();val raw=n.getElementsByTagName("description").item(0)?.textContent.orEmpty();val ex=raw.replace(Regex("<[^>]*>")," ").replace(Regex("\\s+")," ").trim();if(title.isNotBlank()&&link.isNotBlank())stories.add(Story(title,link,ex))}
   runOnUiThread{status.text=if(stories.isEmpty())"No stories found" else "Latest stories";stories.forEachIndexed{i,s->addCard(s,i+1)}}
  }catch(e:Exception){runOnUiThread{status.text="Unable to load stories";list.addView(errorCard())}}}.start()
 }

 private fun addCard(story:Story,number:Int){
  val card=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;background=shape(Color.WHITE,20,1,Color.rgb(229,229,226));setPadding(dp(18),dp(17),dp(18),dp(17))}
  val meta=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL}
  meta.addView(tv("STORY "+number.toString().padStart(2,'0'),10f,gold,true).apply{letterSpacing=.1f},LinearLayout.LayoutParams(0,dp(22),1f))
  val done=posted.getBoolean(story.link,false);if(done)meta.addView(tv("POSTED ✓",10f,green,true).apply{background=shape(Color.rgb(228,239,232),12);setPadding(dp(9),dp(5),dp(9),dp(5))});card.addView(meta)
  card.addView(tv(story.title,19f,ink,true).apply{setPadding(0,dp(6),0,dp(9));maxLines=4})
  val ex=(if(story.excerpt.isBlank())"Read the latest report and discover the full details behind this story." else story.excerpt).take(460)
  card.addView(tv(ex,14f,muted).apply{lineSpacingExtra=dp(2).toFloat();setPadding(0,0,0,dp(16));maxLines=5})
  card.addView(tv("COPY FOR QUORA",13f,Color.WHITE,true).apply{gravity=Gravity.CENTER;background=shape(navy,14);setOnClickListener{val post=story.title+"\n\n"+ex+"\n\nRead the full story:\n"+story.link;(getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Quora post",post));Toast.makeText(this@MainActivity,"Quora post copied",Toast.LENGTH_SHORT).show()}},LinearLayout.LayoutParams(-1,dp(50)))
  val actions=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL;setPadding(0,dp(10),0,0)}
  actions.addView(tv("Open Quora",13f,navy,true).apply{gravity=Gravity.CENTER;setOnClickListener{startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://www.quora.com/")))}},LinearLayout.LayoutParams(0,dp(42),1f))
  actions.addView(tv("│",18f,Color.rgb(226,226,223)).apply{gravity=Gravity.CENTER},LinearLayout.LayoutParams(dp(1),dp(42)))
  actions.addView(tv(if(done)"Posted ✓" else "Mark as posted",13f,if(done)green else muted,true).apply{gravity=Gravity.CENTER;setOnClickListener{posted.edit().putBoolean(story.link,true).apply();text="Posted ✓";setTextColor(green);Toast.makeText(this@MainActivity,"Marked as posted",Toast.LENGTH_SHORT).show()}},LinearLayout.LayoutParams(0,dp(42),1f));card.addView(actions)
  list.addView(card);list.addView(Space(this).apply{minimumHeight=dp(12)})
 }

 private fun errorCard()=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;background=shape(Color.WHITE,20,1,Color.rgb(229,229,226));setPadding(dp(20),dp(22),dp(20),dp(22));addView(tv("We couldn't reach the feed.",18f,ink,true));addView(tv("Check your connection and tap refresh to try again.",14f,muted).apply{setPadding(0,dp(7),0,0)})}
 data class Story(val title:String,val link:String,val excerpt:String)
}