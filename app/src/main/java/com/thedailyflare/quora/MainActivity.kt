package com.thedailyflare.quora

import android.app.Activity
import android.content.*
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray

class MainActivity : Activity() {
 private val feedBase="https://thedailyflare.com/wp-json/wp/v2/posts?per_page=10&_embed=1&_fields=link,title,excerpt,date,tags,_links,_embedded"
 private val quoraSpace="https://thedailyflare.quora.com/"
 private val searchConsoleUrl="https://search.google.com/search-console?utm_source=about-page&resource_id=sc-domain:thedailyflare.com"
 private val posted by lazy { getSharedPreferences("posted", MODE_PRIVATE) }
 private val navy=Color.rgb(23,42,58); private val ink=Color.rgb(32,38,43)
 private val muted=Color.rgb(105,113,120); private val cream=Color.rgb(247,246,243)
 private val gold=Color.rgb(201,168,106); private val green=Color.rgb(71,117,91)
 private lateinit var list:LinearLayout; private lateinit var status:TextView
 private lateinit var swipeRefresh:SwipeRefreshLayout
 private lateinit var searchBox:EditText
 private var allStories:List<Story> = emptyList()
 private var copiedStoryLink:String? = null
 private var refreshPostedOnResume=false
 private val copiedKey="copied_story_link"
 private val instagramCtas=listOf(
  "❤️ Like this one if you want more updates like it.",
  "❤️ If you enjoyed this update, leave a like.",
  "❤️ Give this one a little love. ❤️",
  "🔄 Know someone who'd find this interesting? Send it their way.",
  "🔄 Think someone should see this? Share it with them.",
  "🔄 This one is worth passing along. 🔄",
  "➕ Follow us for more like this.",
  "➕ Stick around — there's more coming. 😉",
  "➕ Follow us so you don't lose us. 😁",
  "📰 Stay informed — follow The Daily Flare for more.",
  "🌍 Keep up with the story — follow for more updates.",
  "📲 Want more updates? Follow The Daily Flare.",
  "👀 Stay tuned for more developments.",
  "📰 More updates are coming — follow The Daily Flare.",
  "🌐 Follow along for the latest developments."
 )
 private fun platformKey(platform:String,link:String)="posted_"+platform+"_"+link

 override fun onCreate(b:Bundle?){
  super.onCreate(b)
  copiedStoryLink=posted.getString(copiedKey,null)
  render()
  loadFeed()
 }
 override fun onResume(){
  super.onResume()
  if(refreshPostedOnResume){
   refreshPostedOnResume=false
   filterStories(searchBox.text?.toString().orEmpty())
  }
 }
 private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
 private fun shape(c:Int,r:Int=0,s:Int=0,sc:Int=Color.TRANSPARENT)=GradientDrawable().apply{setColor(c);cornerRadius=dp(r).toFloat();if(s>0)setStroke(dp(s),sc)}
 private fun tv(v:String,z:Float,c:Int,b:Boolean=false)=TextView(this).apply{text=v;textSize=z;setTextColor(c);if(b)typeface=Typeface.create("sans-serif",Typeface.BOLD)}

 private fun render(){
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(cream)}
  val head=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(22),dp(22),dp(22),dp(18));background=shape(navy)}
  val top=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL}
  top.addView(tv("THE DAILY FLARE",21f,Color.WHITE,true).apply{letterSpacing=.08f},LinearLayout.LayoutParams(0,dp(48),1f))
  head.addView(top);head.addView(tv("SOCIAL SHARING DESK",11f,gold,true).apply{letterSpacing=.12f})
  head.addView(tv("Turn today's stories into ready-to-share posts.",15f,Color.rgb(220,226,230)).apply{setPadding(0,dp(5),0,0)})
  root.addView(head)
  val body=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(18),dp(18),dp(18),dp(8))}
  val row=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL};status=tv("Loading latest stories…",18f,ink,true)
  row.addView(status,LinearLayout.LayoutParams(0,dp(40),1f));row.addView(tv("LIVE",10f,green,true).apply{gravity=Gravity.CENTER;background=shape(Color.rgb(228,239,232),14);setPadding(dp(10),dp(6),dp(10),dp(6))});body.addView(row)
  searchBox=EditText(this).apply{hint="Search recent stories";textSize=15f;setTextColor(ink);setHintTextColor(muted);setSingleLine(true);setPadding(dp(16),0,dp(16),0);background=shape(Color.WHITE,14,1,Color.rgb(225,225,222));addTextChangedListener(object:TextWatcher{override fun beforeTextChanged(s:CharSequence?,st:Int,count:Int,after:Int){};override fun onTextChanged(s:CharSequence?,st:Int,before:Int,count:Int){filterStories(s?.toString().orEmpty())};override fun afterTextChanged(s:Editable?){}})}
  body.addView(searchBox,LinearLayout.LayoutParams(-1,dp(52)).apply{bottomMargin=dp(12)})
  val scroll=ScrollView(this);list=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};scroll.addView(list)
  swipeRefresh=SwipeRefreshLayout(this).apply{setOnRefreshListener{loadFeed()};addView(scroll)}
  body.addView(swipeRefresh,LinearLayout.LayoutParams(-1,0,1f));root.addView(body,LinearLayout.LayoutParams(-1,0,1f))
  root.addView(tv("THE DAILY FLARE  •  NEWS THAT MATTERS",10f,muted,true).apply{gravity=Gravity.CENTER;background=shape(Color.WHITE);setPadding(dp(18),dp(14),dp(18),dp(16));letterSpacing=.08f})
  setContentView(root)
 }

 private fun loadFeed(){
  if(::swipeRefresh.isInitialized) swipeRefresh.isRefreshing=true
  status.text="Refreshing recent stories…";list.removeAllViews()
  Thread{
   try{
    val calendar=Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply{add(Calendar.DAY_OF_YEAR,-3)}
    val formatter=SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US).apply{timeZone=TimeZone.getTimeZone("UTC")}
    val after=formatter.format(calendar.time)
    val feed=feedBase+"&after="+java.net.URLEncoder.encode(after,"UTF-8")
    val connection=URL(feed).openConnection() as HttpURLConnection
    connection.connectTimeout=15000;connection.readTimeout=15000
    connection.setRequestProperty("User-Agent","DailyFlareQuora/1.0");connection.connect()
    if(connection.responseCode !in 200..299) throw Exception("HTTP "+connection.responseCode)
    val json=connection.inputStream.bufferedReader().use{it.readText()}
    val posts=JSONArray(json);val stories=mutableListOf<Story>()
    for(i in 0 until posts.length()){
     val post=posts.getJSONObject(i)
     val title=stripHtml(post.getJSONObject("title").optString("rendered"))
     val link=post.optString("link")
     val excerpt=stripHtml(post.getJSONObject("excerpt").optString("rendered"))
     val tags=extractPostTags(post)
     if(title.isNotBlank()&&link.isNotBlank())stories.add(Story(title,link,excerpt,tags))
    }
    connection.disconnect()
    runOnUiThread{allStories=stories;filterStories(searchBox.text?.toString().orEmpty());if(::swipeRefresh.isInitialized)swipeRefresh.isRefreshing=false}
   }catch(e:Exception){runOnUiThread{status.text="Unable to load stories";list.addView(errorCard());if(::swipeRefresh.isInitialized)swipeRefresh.isRefreshing=false}}
  }.start()
 }

 private fun extractPostTags(post:org.json.JSONObject):List<String>{
  val result=mutableListOf<String>()
  val embedded=post.optJSONObject("_embedded") ?: return result
  val terms=embedded.optJSONArray("wp:term") ?: return result
  for(i in 0 until terms.length()){
   val group=terms.optJSONArray(i) ?: continue
   for(j in 0 until group.length()){
    val term=group.optJSONObject(j) ?: continue
    if(term.optString("taxonomy")!="post_tag") continue
    val name=stripHtml(term.optString("name")).trim()
    if(name.isNotBlank()&&!result.contains(name))result.add(name)
   }
  }
  return result.take(3)
 }

 private fun filterStories(query:String){
  val q=query.trim().lowercase(Locale.getDefault())
  val filtered=if(q.isBlank())allStories else allStories.filter{it.title.lowercase(Locale.getDefault()).contains(q)||it.excerpt.lowercase(Locale.getDefault()).contains(q)}
  status.text=when{allStories.isEmpty()->"No recent stories";q.isNotBlank()&&filtered.isEmpty()->"No matches found";else->"Recent stories"}
  list.removeAllViews();filtered.forEachIndexed{i,s->addCard(s,i+1)}
  if(filtered.isEmpty()&&allStories.isNotEmpty())list.addView(emptySearchCard())
 }

 private fun stripHtml(value:String)=value.replace(Regex("<[^>]*>")," ").replace("&nbsp;"," ").replace("&amp;","&").replace("&#8217;","'").replace(Regex("\\s+")," ").trim()

 private fun hashtag(tag:String):String{
  val clean=tag.trim().removePrefix("#")
  val compact=clean.replace(Regex("[^\\p{L}\\p{N}]"),"")
  return if(compact.isBlank())"" else "#"+compact
 }

 private fun instagramPost(excerpt:String,tags:List<String>):String{
  val cta=instagramCtas.random()
  val hashtags=tags.take(3).mapNotNull{hashtag(it).takeIf{h->h.isNotBlank()}}
  val parts=mutableListOf<String>()
  parts.add(excerpt)
  parts.add(cta)
  if(hashtags.isNotEmpty())parts.add(hashtags.joinToString(" "))
  return parts.joinToString("\n\n")
 }

 private fun copyInstagramAndOpen(story:Story,ex:String){
  val text=instagramPost(ex,story.tags)
  (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Instagram post",text))
  Toast.makeText(this,"Instagram caption copied",Toast.LENGTH_SHORT).show()
  showInstagramChoice()
 }

 private fun showInstagramChoice(){
  android.app.AlertDialog.Builder(this)
   .setTitle("Instagram")
   .setItems(arrayOf("Post", "Message")){_,which->
    if(which==0)openInstagram("instagram://camera") else openInstagram("instagram://direct-inbox")
   }
   .setNegativeButton("Cancel",null)
   .show()
 }

 private fun openInstagram(uri:String){
  try{
   val intent=Intent(Intent.ACTION_VIEW,Uri.parse(uri)).apply{setPackage("com.instagram.android")}
   if(intent.resolveActivity(packageManager)!=null)startActivity(intent)
   else{
    val launch=packageManager.getLaunchIntentForPackage("com.instagram.android")
    if(launch!=null)startActivity(launch)else startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://www.instagram.com/")))
   }
  }catch(e:Exception){
   try{
    val launch=packageManager.getLaunchIntentForPackage("com.instagram.android")
    if(launch!=null)startActivity(launch)else startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://www.instagram.com/")))
   }catch(_:Exception){startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://www.instagram.com/")))}
  }
 }

 private fun addCard(story:Story,number:Int){
  val card=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;background=shape(Color.WHITE,20,1,Color.rgb(229,229,226));setPadding(dp(18),dp(17),dp(18),dp(17))}
  val meta=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL}
  meta.addView(tv("STORY "+number.toString().padStart(2,'0'),10f,gold,true).apply{letterSpacing=.1f},LinearLayout.LayoutParams(0,dp(22),1f))
  val done=posted.getBoolean(story.link,false)
  if(done)meta.addView(tv("POSTED ✓",10f,green,true).apply{background=shape(Color.rgb(228,239,232),12);setPadding(dp(9),dp(5),dp(9),dp(5))})
  val titleCopy=tv("⧉",15f,navy,true).apply{gravity=Gravity.CENTER;contentDescription="Copy article title";setPadding(dp(7),0,dp(7),0)}
  titleCopy.setOnClickListener{(getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Article title",story.title));Toast.makeText(this@MainActivity,"Article title copied",Toast.LENGTH_SHORT).show()}
  meta.addView(titleCopy,LinearLayout.LayoutParams(dp(36),dp(30)));card.addView(meta)
  card.addView(tv(story.title,19f,ink,true).apply{setPadding(0,dp(6),0,dp(14));maxLines=3})
  val ex=(if(story.excerpt.isBlank())"Read the latest report and discover the full details behind this story." else story.excerpt).take(460)
  val quoraPost=ex+"\n\n"+story.link
  val socialRows=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
  val row1=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL}
  val row2=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL;setPadding(0,dp(7),0,0)}
  fun socialIcon(label:String,bg:Int,platform:String,action:()->Unit):TextView{
   val icon=tv(label,15f,Color.WHITE,true).apply{gravity=Gravity.CENTER}
   fun paint(done:Boolean){icon.background=shape(if(done)Color.rgb(175,178,180) else bg,16);icon.alpha=if(done).55f else 1f}
   paint(posted.getBoolean(platformKey(platform,story.link),false))
   icon.setOnClickListener{action();posted.edit().putBoolean(platformKey(platform,story.link),true).apply();paint(true)}
   return icon
  }
  fun addSocial(row:LinearLayout,icon:TextView){if(row.childCount>0)row.addView(Space(this).apply{minimumWidth=dp(7)});row.addView(icon,LinearLayout.LayoutParams(0,dp(46),1f))}
  addSocial(row1,socialIcon("Q",Color.rgb(185,43,39),"quora"){
   val pending=copiedStoryLink ?: posted.getString(copiedKey,null)
   if(pending==story.link&&!posted.getBoolean(story.link,false)){posted.edit().putBoolean(story.link,true).apply();refreshPostedOnResume=true;Toast.makeText(this@MainActivity,"Marked as posted",Toast.LENGTH_SHORT).show()}
   startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(quoraSpace)))
  })
  addSocial(row1,socialIcon("f",Color.rgb(24,119,242),"facebook"){
   (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Facebook post",ex+"\n\n"+story.link))
   Toast.makeText(this@MainActivity,"Facebook text copied — paste it into your post",Toast.LENGTH_LONG).show()
   try{val launch=packageManager.getLaunchIntentForPackage("com.facebook.katana");if(launch!=null)startActivity(launch)else startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://www.facebook.com/")))}catch(e:Exception){startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://www.facebook.com/")))}
  })
  addSocial(row1,socialIcon("𝕏",Color.rgb(25,25,25),"x"){shareToApp("com.twitter.android",ex+"\n\n"+story.link,"X")})
  addSocial(row1,socialIcon("@",Color.rgb(35,35,35),"threads"){shareToApp("com.instagram.barcelona",ex+"\n\n"+story.link,"Threads")})
  addSocial(row2,socialIcon("t",Color.rgb(52,70,93),"tumblr"){shareToApp("com.tumblr",ex+"\n\n"+story.link,"Tumblr")})
  addSocial(row2,socialIcon("in",Color.rgb(10,102,194),"linkedin"){shareToApp("com.linkedin.android",ex+"\n\n"+story.link,"LinkedIn")})
  addSocial(row2,socialIcon("G",Color.rgb(66,133,244),"search_console"){copyArticleUrlAndOpenSearchConsole(story)})
  addSocial(row2,socialIcon("◎",Color.rgb(193,53,132),"instagram"){copyInstagramAndOpen(story,ex)})
  socialRows.addView(row1);socialRows.addView(row2);card.addView(socialRows)
  val actions=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL;setPadding(0,dp(10),0,0)}
  val copyButton=tv("Copy for Quora",13f,navy,true).apply{gravity=Gravity.CENTER}
  val markButton=tv(if(done)"Posted ✓" else "Mark as posted",13f,if(done)green else muted,true).apply{gravity=Gravity.CENTER}
  copyButton.setOnClickListener{
   (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Quora post",quoraPost));copiedStoryLink=story.link;posted.edit().putString(copiedKey,story.link).apply();Toast.makeText(this@MainActivity,"Copied — now tap Q to open Quora",Toast.LENGTH_SHORT).show()
  }
  markButton.setOnClickListener{posted.edit().putBoolean(story.link,true).apply();markButton.text="Posted ✓";markButton.setTextColor(green);Toast.makeText(this@MainActivity,"Marked as posted",Toast.LENGTH_SHORT).show()}
  actions.addView(copyButton,LinearLayout.LayoutParams(0,dp(42),1f));actions.addView(tv("│",18f,Color.rgb(226,226,223)).apply{gravity=Gravity.CENTER},LinearLayout.LayoutParams(dp(1),dp(42)));actions.addView(markButton,LinearLayout.LayoutParams(0,dp(42),1f));card.addView(actions)
  list.addView(card);list.addView(Space(this).apply{minimumHeight=dp(12)})
 }

 private fun copyArticleUrlAndOpenSearchConsole(story:Story){
  (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Article URL",story.link))
  try{startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(searchConsoleUrl)))}catch(e:Exception){startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://search.google.com/search-console/")))}
 }

 private fun shareViaTarget(packageName:String,text:String,label:String){
  val send=Intent(Intent.ACTION_SEND).apply{type="text/plain";putExtra(Intent.EXTRA_TEXT,text)}
  val matches=packageManager.queryIntentActivities(send,0);val target=matches.firstOrNull{it.activityInfo.packageName==packageName}
  if(target!=null){send.component=android.content.ComponentName(target.activityInfo.packageName,target.activityInfo.name);try{startActivity(send)}catch(e:Exception){Toast.makeText(this,label+" is not available on this device.",Toast.LENGTH_LONG).show()}}else Toast.makeText(this,label+" is not available on this device.",Toast.LENGTH_LONG).show()
 }

 private fun shareToApp(packageName:String,text:String,label:String){
  val send=Intent(Intent.ACTION_SEND).apply{type="text/plain";putExtra(Intent.EXTRA_TEXT,text);setPackage(packageName)}
  try{startActivity(send)}catch(e:Exception){Toast.makeText(this,label+" is not available on this device.",Toast.LENGTH_LONG).show()}
 }

 private fun emptySearchCard()=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;background=shape(Color.WHITE,20,1,Color.rgb(229,229,226));setPadding(dp(20),dp(22),dp(20),dp(22));addView(tv("No matching recent story.",18f,ink,true));addView(tv("Try a different keyword or clear your search.",14f,muted).apply{setPadding(0,dp(7),0,0)})}
 private fun errorCard()=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;background=shape(Color.WHITE,20,1,Color.rgb(229,229,226));setPadding(dp(20),dp(22),dp(20),dp(22));addView(tv("We couldn't load the latest stories.",18f,ink,true));addView(tv("Please check your connection and pull down to refresh.",14f,muted).apply{setPadding(0,dp(7),0,0)})}
 data class Story(val title:String,val link:String,val excerpt:String,val tags:List<String>)
}
