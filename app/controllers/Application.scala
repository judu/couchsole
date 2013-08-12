package controllers

import play.api._
import play.api.mvc._
import play.api.i18n.Messages
import util.Secured

object Application extends Controller with Secured {
	import play.api.data._
	import play.api.data.Forms._
	import datasources.SpecialCouchbaseClient._
	import scala.concurrent.ExecutionContext.Implicits.global
	import play.api.libs.json.Json

	def indexPage = Sessioned { sr =>
		Ok(views.html.pages.home())
	}

	def documentsPage = Sessioned { sr =>
		Ok(views.html.pages.documents())
	}

	val createForm = Form(
		tuple(
			"id" -> text,
			"data" -> text
		)
	)

	def documents = Sessioned { implicit sr =>
		Async {
			val qty = sr.queryString.get("quantity")
				.flatMap(_.headOption)
				.map(_.toInt).getOrElse(10)
			val page = sr.queryString.get("page")
				.flatMap(_.headOption)
				.map(_.toInt).getOrElse(0)
			getDocuments(sr.infos,qty*page,qty).map(docs => Ok(Json.toJson(docs)).withHeaders("Content-Type" -> "application/json"))
		}
	}

	def cbviews = TODO

	def createDocument = Sessioned { implicit sr =>
		val (id,data) = createForm.bindFromRequest.get
		sr.infos.client.set(id,0,data)
		NoContent
	}

}
