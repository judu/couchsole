package util

import play.api._
import play.api.mvc._, Results._
import com.couchbase.client.CouchbaseClient
import datasources.CouchbaseManager.CouchbaseInfos


case class SessionedRequest[A](
	infos: CouchbaseInfos, private val request: Request[A]
) extends WrappedRequest(request)

trait Secured {
	import datasources.CouchbaseManager

	def Sessioned[A](p: BodyParser[A])(f: SessionedRequest[A] => Result): Action[A] = {
		Action(p) { request =>
			val result = for {
				tok <- request.session.get("conntok")
				infos <- Option(CouchbaseManager.getConnection(tok))
			} yield f(SessionedRequest(infos, request))
			result.fold({
				val req = for {
					uri <- request.session.get("dburi")
					bucket <- request.session.get("dbbucket")
					password <- request.session.get("dbpassword")
					token <- Option(CouchbaseManager.connect(uri,bucket,password))
					infos <- Option(CouchbaseManager.getConnection(token))
				} yield  SessionedRequest(infos, request)
				req.fold({
					val res: play.api.mvc.Result = SeeOther(controllers.routes.Welcome.login.url)
					res
				})(
					rreq => f(rreq).withSession(
						"conntok" -> rreq.infos.token,
						"dburi" -> rreq.infos.uri,
						"dbbucket" -> rreq.infos.bucket,
						"dbpassword" -> rreq.infos.password
					)
				)
			})(identity)
		}
	}

  import play.api.mvc.BodyParsers._
  def Sessioned(f: SessionedRequest[AnyContent] => Result): Action[AnyContent]  = {
	  Sessioned(parse.anyContent)(f)
  }
}
