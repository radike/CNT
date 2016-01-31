## Inspiration

Imagine a situation when you want to transfer large data set files. All current solutions pose significant limitations: file size, budget, user friendliness, or middle copy necessity.

## What it does

CNT successfully transfers files between clients without creating a middle copy enabling the server to scale easily with number of connections.

## How we built it

The clients are build in Java - therefore, exhibit multiplatform coverage. The server is written in Go exploiting the concurrency concepts.

Created as a hack at [Hack Cambridge 2016](https://www.hackcambridge.com)
