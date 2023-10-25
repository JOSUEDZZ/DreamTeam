package mx.tecnm.lapaz.ubertec

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

const val url = "http://192.168.0.23/tienda/public"

class WebServiceREST {
    companion object {
        suspend fun get(urlString: String): String {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            val response = StringBuffer()
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            var inputLine = reader.readLine()
            while (inputLine != null) {
                response.append(inputLine)
                inputLine = reader.readLine()
            }
            reader.close()
            return response.toString()
        }

        /** Ejemplo del parametro postdata
         postdata="nombre=chilaquiles&descripcion=tortillas%20en%20triangulos"
         */
        suspend fun post(urlString: String, postData: String):String {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(postData)
            writer.flush()
            writer.close()
            val responseCode = connection.responseCode
            println("Response code: $responseCode")
            val response = StringBuffer()
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            var inputLine = reader.readLine()
            while (inputLine != null) {
                response.append(inputLine)
                inputLine = reader.readLine()
            }
            reader.close()
            return response.toString()
        }

        suspend fun postFile(urlString: String, parametros: List<Parametro>,
                             inputFileName:String, fileName:String,
                             fileInputStream: FileInputStream):String {
            val boundary = "===" + System.currentTimeMillis() + "===" // límite para separar los diferentes campos en la solicitud
            val lineEnd = "\r\n"
            val twoHyphens = "--"

            val connection = URL(urlString).openConnection() as HttpURLConnection
            connection.doInput = true
            connection.doOutput = true
            connection.useCaches = false
            connection.requestMethod = "POST"
            connection.setRequestProperty("Connection", "Keep-Alive")
            connection.setRequestProperty("ENCTYPE", "multipart/form-data")
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=$boundary")

            val outputStream = DataOutputStream(connection.outputStream)

            // adjuntar los parámetros de texto al cuerpo de la solicitud
            for (parametro in parametros) {
                outputStream.writeBytes("$twoHyphens$boundary$lineEnd")
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + parametro.key + "\"" + lineEnd)
                outputStream.writeBytes(lineEnd)
                outputStream.writeBytes(parametro.valor)
                outputStream.writeBytes(lineEnd)
            }

            // adjuntar el archivo al cuerpo de la solicitud
            outputStream.writeBytes("$twoHyphens$boundary$lineEnd")
            outputStream.writeBytes("Content-Disposition: form-data; name=\""+inputFileName+"\";filename=\"" + fileName + "\"" + lineEnd)
            outputStream.writeBytes(lineEnd)

            //val fileInputStream = FileInputStream(file)
            val buffer = ByteArray(1024)
            var bytesRead = 0
            while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.writeBytes(lineEnd)
            outputStream.writeBytes("$twoHyphens$boundary$twoHyphens$lineEnd")

            val responseText = connection.inputStream.bufferedReader().use { it.readText() }

            // cerrar los flujos y desconectar la conexión
            fileInputStream.close()
            outputStream.flush()
            outputStream.close()
            connection.disconnect()
            return responseText
        }

        suspend fun getImage(urlString: String): Bitmap? {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            //connection.getHeaderField("Last-Modified")
            connection.requestMethod = "GET"
            val contentType = connection.getHeaderField("Content-Type")
            if (contentType.startsWith("image")) {
                return BitmapFactory.decodeStream(connection.inputStream)
            }
            return null
        }
    }
}