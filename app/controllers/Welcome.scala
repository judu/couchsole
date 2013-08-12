package controllers

import play.api._
import play.api.mvc._

object Welcome extends Controller {
	import play.api.data._
	import play.api.data.Forms._

	val loginForm = Form(
		tuple(
			"hostname" -> text,
			"port" -> number,
			"bucket" -> text,
			"password" -> text
		)
	)

	def login = Action {
		Ok(views.html.Welcome.login(loginForm))
	}

	def auth = Action { implicit request =>
		import datasources.CouchbaseManager
		val (h,po,b,pa) = loginForm.bindFromRequest.get
		val token = Option(CouchbaseManager.connect(h,po,b,pa))
		token.fold(BadRequest(views.html.Welcome.login(loginForm)).withNewSession)(tok => {
			val infos = CouchbaseManager.getConnection(tok)
			SeeOther(routes.Application.page("home").url).withSession(
				"conntok" -> tok,
				"dburi" -> infos.uri,
				"dbbucket" -> b,
				"dbpassword" -> pa
			)
		})
	}
}
