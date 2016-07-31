package swordfishsync

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(controller:"Index")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
