package datasources

object SpecialCouchbaseClient {
	import CouchbaseManager.CouchbaseInfos
	import play.api.libs.ws._
	import play.api.libs.json._
	import scala.concurrent.Future
	import com.ning.http.client.Realm.AuthScheme
	import scala.concurrent.ExecutionContext.Implicits.global

	case class Document(id: String, `type`: String, doc: String)
	implicit val gar: Format[Document] = Json.format[Document]

	case class CBView(id: String, map: String, reduce: Option[String])
	implicit val cbar: Format[CBView] = Json.format[CBView]
	case class DesignDocument(
		id: String,
		views: Seq[CBView],
		production: Boolean = false
	)
	implicit val ddocfmt: Format[DesignDocument] = Json.format[DesignDocument]

	def getDocuments(infos: CouchbaseInfos, skip: Int = 0, limit: Int = 10): Future[Seq[Document]] = {
		val uri = infos.uri.substring(0,infos.uri.lastIndexOf("/")+1) +
			"couchBase/" + infos.bucket + "/_all_docs?skip="+skip.toString +
			"&limit=" + limit.toString + "&include_docs=true"
		WS.url(uri).withAuth(infos.bucket,infos.password,AuthScheme.BASIC).get.map(r => {
			val parsed = Json.parse(r.body)
			(parsed \ "rows") match {
				case JsArray(rows) => rows.flatMap(doc => (doc \ "id") match {
						case JsString(id) => {
							val jsonDoc = (doc \ "doc" \ "json") match {
								case JsUndefined(_) => None
								case a => Some(Json.stringify(a))
							}
							val base64Doc = (doc \ "doc" \ "base64") match {
								case JsString(b64) => Some(b64)
								case _ => None
							}

							jsonDoc.fold(base64Doc.map(
								d => Document(
									id,
									"base64",
									d
								))
							)(
								jsd => Some(Document(
									id,
									"json",
									jsd
								))
							)
						}
						case _ => None
					})
				case _ => Seq.empty
			}
		})
	}

	def getDesignDocuments(infos: CouchbaseInfos): Future[Seq[DesignDocument]] = {
		val uri = infos.uri + "/default/buckets/" + infos.bucket + "/ddocs"
		WS.url(uri).withAuth(infos.bucket,infos.password,AuthScheme.BASIC).get.map(r => {
			val parsed = Json.parse(r.body)
			(parsed \ "rows") match {
				case JsArray(rows) => rows.flatMap(row => (row \ "doc") match {
					case JsObject(fields) => {
						val mfields = fields.toMap
						val id = mfields.get("meta").flatMap(m => (m \ "id") match {
							case JsString(str) => Some(str)
							case _ => None
						})
						val cbviews = mfields.get("json").map(j => (j \ "views") match {
							case JsObject(jsonviews) => jsonviews.flatMap(p => {
								val cbmap = (p._2 \ "map") match {
									case JsString(str) => Some(str)
									case _ => None
								}
								val cbreduce = (p._2 \ "reduce") match {
									case JsString(str) => Some(str)
									case _ => None
								}
								cbmap.map(m => CBView(p._1,m,cbreduce))
							})
							case _ => Seq.empty
						})
						for{
							i <- id
							vs <- cbviews
						} yield DesignDocument(i,vs,i.contains("/dev_"))
					}
					case _ => None
				})
				case _ => Seq.empty
			}
		})

	}
}

