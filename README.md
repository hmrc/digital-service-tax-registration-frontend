
# digital-service-tax-registration-frontend

## About
The Digital Services Tax (DST) digital service is split into a number of different microservices all serving specific functions which are listed below:

**Frontend** - The main frontend for the service which includes the pages for registration.

**Backend** - The service that the frontend uses to call HOD APIs to retrieve and send information relating to business information and subscribing to regime.

**Stub** - Microservice that is used to mimic the DES APIs when running services locally or in the development and staging environments.

This is the main frontend, currently containing the registration form.

For details about the digital services tax see [the GOV.UK guidance](https://www.gov.uk/government/consultations/digital-services-tax-draft-guidance)

## Running the service
### Service manager
*You need to be on the VPN*
Ensure your service manager config is up to date.

The whole service can be started with:

`sm2 --start DST_ALL`

or specifically for only the frontend

`sm2 --start DST_FRONTEND`

This will start all the required services.

### Locally

`sbt 'run 8740'` or `./run.sh`

* Visit http://localhost:9949/auth-login-stub/gg-sign-in
* You may need to add some user details to the form:
#### DST Registration journey
    * Affinity Group: Organisation
    * Enrolment Key: IR-CT
    * Identifier Name: UTR
    * Identifier Value: 1111111000
* Then enter a redirect url: http://localhost:8740/digital-services-tax
* Press **Submit**.

## Running the tests

  sbt test it/test

## Running scalafmt

To apply scalafmt formatting using the rules configured in the .scalafmt.conf, run:

`sbt scalafmtAll`

To check the files have been formatted correctly, run:

`sbt scalafmtCheckAll`

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").