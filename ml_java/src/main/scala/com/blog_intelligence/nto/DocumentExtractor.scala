package com.blog_intelligence.nto

import java.io.File

case class DataBaseConfiguration(
	host: String,
	port: String,
	username: String,
	password: String,
	database: String
);

class DocumentExtractor {

	def readFromCSV(file: File): Unit = {
		println("I am doing nothing.")
	}

	def readFromDB(host: String, port: Int, username: String, pwd: String): Unit = {
		println("I am doing nothing.")
	}
}
