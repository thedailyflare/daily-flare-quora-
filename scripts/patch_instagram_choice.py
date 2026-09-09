from pathlib import Path

p = Path('app/src/main/java/com/thedailyflare/quora/MainActivity.kt')
s = p.read_text()
old = ''' private fun copyInstagramAndOpen(story:Story,ex:String){
  val text=instagramPost(ex,story.tags)
  (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Instagram post",text))
  Toast.makeText(this,"Instagram post copied — paste it into Instagram",Toast.LENGTH_LONG).show()
  try{
   val launch=packageManager.getLaunchIntentForPackage("com.instagram.android")
   if(launch!=null)startActivity(launch)else startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://www.instagram.com/")))
  }catch(e:Exception){startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://www.instagram.com/")))}
 }
'''
new = ''' private fun copyInstagramAndOpen(story:Story,ex:String){
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
'''
if old not in s:
    raise SystemExit('Expected Instagram function not found; refusing to change anything.')
p.write_text(s.replace(old, new, 1))
