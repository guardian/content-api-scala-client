### content-api-client-aws
A library for helping with requests to an IAM-authorised AWS api-gateway.

Creates the necessary authorisation headers based on a request.
E.g.
```
import com.gu.contentapi.client.{IAMSigner, IAMEncoder}

val signer = new IAMSigner(credentialsProvider, awsRegion))

//Query params must be encoded correctly
val queryString = IAMEncoder.encodeParams("testparam=with spaces")

val uri = URI.create(s"$endpoint/$path?$queryString")

val headers: Map[String,String] = signer.addIAMHeaders(Map.empty[String,String], uri)

//...create a request with uri and headers
```