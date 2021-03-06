import sbt._
import Keys._
import org.scalatra.sbt._
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._
import com.earldouglas.xsbtwebplugin.PluginKeys._
import com.earldouglas.xsbtwebplugin.WebPlugin._

object N20DemoBuild extends Build {
  val Organization = "de.hpi.smm"
  val Name = "N20 Demo"
  val Version = "0.1.0-SNAPSHOT"
  val ScalaVersion = "2.11.1"
  val ScalatraVersion = "2.3.0"

  lazy val project = Project (
    "n20-demo",
    file("."),
    settings = ScalatraPlugin.scalatraWithJRebel ++ scalateSettings ++ Seq(
      port in container.Configuration := 9001,
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := ScalaVersion,
      resolvers += Classpaths.typesafeReleases,
      libraryDependencies ++= Seq(
        "org.scalatra" %% "scalatra" % ScalatraVersion,
        "org.scalatra" %% "scalatra-scalate" % ScalatraVersion,
        "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test",
        "ch.qos.logback" % "logback-classic" % "1.1.2" % "runtime",
        "org.eclipse.jetty" % "jetty-webapp" % "9.1.5.v20140505" % "container",
        "org.eclipse.jetty" % "jetty-plus" % "9.1.5.v20140505" % "container",
        "javax.servlet" % "javax.servlet-api" % "3.1.0",
        "org.scalatra" %% "scalatra-json" % "2.3.0",
        "org.json4s"   %% "json4s-jackson" % "3.2.9",
        "com.lambdaworks" % "jacks_2.11" % "2.3.3",
		"net.sf.opencsv" % "opencsv" % "2.3",
		"edu.stanford.nlp" % "stanford-corenlp" % "3.4.1",
		"edu.stanford.nlp" % "stanford-corenlp" % "3.4.1" classifier "models",
		"org.apache.lucene" % "lucene-core" % "4.8.1",
		"org.apache.lucene" % "lucene-analyzers-common" % "4.8.1",
        "nz.ac.waikato.cms.weka" % "weka-dev" % "3.7.6"
      ),
      scalateTemplateConfig in Compile <<= (sourceDirectory in Compile){ base =>
        Seq(
          TemplateConfig(
            base / "webapp" / "WEB-INF" / "templates",
            Seq.empty,  /* default imports should be added here */
            Seq(
              Binding("context", "_root_.org.scalatra.scalate.ScalatraRenderContext", importMembers = true, isImplicit = true)
            ),  /* add extra bindings here */
            Some("templates")
          )
        )
      }
    )
  )
}
