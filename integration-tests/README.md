# MacStadium Jenkins Plugin Integration Tests

## Tools

The MacStadium Jenkins plugin tests use NodeJs, [Jest](https://jestjs.io/docs/en/getting-started) and [Playwright](https://github.com/microsoft/playwright).

## Prerequisites

1. Make sure you have Docker running
2. Connect via VPN to the environment against which you want to run Jenkins tests
3. Make sure the environment against which the tests run have a proper image created with Java installed and SSH enabled. If the image name is different than `jenkins.img`, pass the correct name via config option `VM_BASE_IMAGE` (see Config options below)

## Setup Jenkins & Run Tests

Run `npm run test`. A pretest script will setup Jenkins for you in Docker container and a posttest one will stop it and remove the container.

NOTE: Test user and proper VM config are created during the tests setup and cleared during the tests teardown.

## Config options

The tests can be started with various config options. All of them have default values which are used if no other is provided.

| Option          | Descriptipion                                            | Default Value                     |
| --------------- | -------------------------------------------------------- | --------------------------------- |
| JENKINS_URL     | The URL to access Jenkins UI                             | http://localhost:8080     |
| JENKINS_API_URL | The URL to access Jenkins API along with its credentials | http://admin:admin@localhost:8080 |
| RUN_HEADLESS    | Set if the tests should run using headless browser       | true                              |
| API_URL         | Orka API URL                                             | http://10.221.188.100             |
| VM_BASE_IMAGE   | The base image to be used for Jenkins cloud agents       | jenkins.img                       |
| LICENSE_KEY     | Orka license key                                         | orka-license-key                  |

### Usage

`RUN_HEADLESS=false npm run test`

`ORKA_TEST_USER=myuser@test.com npm run test`
