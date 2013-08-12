package controllers

import play.api._
import play.api.mvc._
import play.api.i18n.Messages
import util.Secured
import navigation.{pages,Item,Menu}

case class PageContext(menu: Menu, activeItemOrTitle: Either[Item, String])

object Application extends Controller with Secured {
	import play.api.data._
	import play.api.data.Forms._
	import datasources.SpecialCouchbaseClient._
	import scala.concurrent.ExecutionContext.Implicits.global
	import play.api.libs.json.Json

	implicit val menu = Menu.fromPageKeys(pages.keys)

	def page(path: String) = Sessioned { sr =>
		(menu.get(path) map { case (page, item) => (page, Left(item)) }) orElse (pages.get(path) map ((_, Right(Messages(s"$path.title"))))) match {
			case Some((page, itemOrTitle)) => Ok(page.render(PageContext(menu, itemOrTitle)))
			case None => NotFound
		}
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
