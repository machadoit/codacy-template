package codacy.sqlint

import codacy.docker.api.Result.Issue
import codacy.docker.api.utils.ToolHelper
import codacy.docker.api.{Pattern, Result, Source, Tool}
import codacy.dockerApi.utils.CommandRunner

import scala.util.{Failure, Properties, Success, Try}

object SQLint extends Tool {

  //sbt "set version in Docker := \"latest\"" "set name := \"$(basename `pwd`)\"" docker:publishLocal
  //docker run -v $(pwd):/src codacy-template
  override def apply(source: Source.Directory, configuration: Option[List[Pattern.Definition]], files: Option[Set[Source.File]])
                    (implicit specification: Tool.Specification): Try[List[Result]] = {
    Try {
      val path = new java.io.File(source.path)
      val filesToLint: Set[String] = ToolHelper.filesToLint(source, files)

      val command = List("echo", "hello!")

      CommandRunner.exec(command, Some(path)) match {
        case Right(resultFromTool) if resultFromTool.exitCode < 2 =>
          Success(parseToolResult(resultFromTool.stdout))
        case Right(resultFromTool) =>
          val msg =
            s"""
               |Tool exited with code ${resultFromTool.exitCode}
               |stdout: ${resultFromTool.stdout.mkString(Properties.lineSeparator)}
               |stderr: ${resultFromTool.stderr.mkString(Properties.lineSeparator)}
                """.stripMargin
          Failure(new Exception(msg))
        case Left(e) =>
          Failure(e)
      }
    }.flatten
  }

  private def parseToolResult(outputLines: List[String]): List[Result] = {
    println("\nDocker debug:\n")
    println(s"\n${outputLines.mkString("\n")}\n")
    List(Issue(
      Source.File("!!hello!!"),
      Result.Message("!!message!!"),
      Pattern.Id("!!patternId!!"),
      Source.Line(1)
    ))
  }

}
