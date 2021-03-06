var _ = require('underscore')
const request = require('request');
var mongoose = require('mongoose');
var association = require("./models/association");
var capteur = require("./models/capteur");
var depot = require("./models/depot");
var poubelle = require("./models/poubelle");
var releve = require("./models/releve");
var decheterie = require("./models/decheterie");
var utilisateur = require("./models/utilisateur");
mongoose.Promise = global.Promise;
// mongoose.connect('mongodb://localhost:27017/smart_db'); // connect to our database
// in order to populate real database, please uncomment following line:
mongoose.connect('mongodb://admin:admin@ds147459.mlab.com:47459/smart_db'); // connect to our database

const real_capteur_id = new mongoose.mongo.ObjectId("5add92e9e36c0adec0253805")

function create_dummy_sensor(trash_id) {
    // Add default sensor
    capteur.create({
        _id: new mongoose.mongo.ObjectId(),
        tokenCapteur: Math.floor(Math.random() * 100000000000000000000),
        idPoubelle: trash_id
    });
}

function create_dummy_users(assoc_id1, assoc_id2) {
    // Add default users
    var user_id1 = new mongoose.mongo.ObjectId();
    utilisateur.create({
        _id: user_id1,
        mail: "jeanjeacques@goldman",
        motDePasse: "$2a$08$Yg9S0Zmc09BBCxUz4XStRuAQHpvtCDANfkIHfPraTD1sOfgTJmh1u", // test
        nom: "JJG",
        adresse: "2 rue de la fleur",
        dateNaissance: "1976-10-25 00:00:00.000",
        sexe: 0,
        idAssoc: assoc_id1,
    });
    var user_id2 = new mongoose.mongo.ObjectId();
    utilisateur.create({
        _id: user_id2,
        mail: "bettancourt@loreal.com",
        motDePasse: "$2a$08$BJbXlbc8k3/M2FqsQSfQ6Opk6Db26EdNbEVGO/VYgCxXosLzLnmke", // argent
        nom: "Betty",
        adresse: "2 place bellecour",
        dateNaissance: "1971-10-25 00:00:00.000",
        sexe: 1,
        idAssoc: assoc_id2,
    });
    var user_id3 = new mongoose.mongo.ObjectId();
    utilisateur.create({
        _id: user_id3,
        mail: "webadmin",
        motDePasse: "$2a$08$/xZaUhauUcgXsZB3nACr6.Ef7ydb2b2iCAORZbDuVV5xW09.Ms9w.", // pwd
        nom: "Billy",
        adresse: "69 rue des abricots frais",
        dateNaissance: "1992-01-13 00:00:00.000",
        sexe: 0,
        isAdmin: true,
    });


    /** for (var i = 0; i < 712; i++) {
        utilisateur.create({
            _id: new mongoose.mongo.ObjectId(),
            mail: "user" + i + "@gmail.com",
            motDePasse: "$2a$08$VeK9ZE4upDhhW6.ftfQw8usgpUiNapgOg50eZ3XFYZi8DDKaO2CZW",
            nom: "user#" + i,
            adresse: "2 rue de la fleur apart " + i,
            dateNaissance: "1976-10-25 00:00:00.000",
            sexe: i % 2,
            idAssoc: i % 2 == 0 ? assoc_id1 : assoc_id2,
        });
    } */

}

function populate_db() {
    // Drop all collections
    var collections = _.keys(mongoose.connection.collections)
    collections.forEach(element => {
        var col = mongoose.connection.collections[element];
        col.drop(function (err) {
            if (err)
                console.log(err);
        });
    });

    // Populate database from GrandLyon trash locations
    trash_dataset_download_link = "https://download.data.grandlyon.com/wfs/grandlyon?SERVICE=WFS&VERSION=2.0.0&outputformat=GEOJSON&maxfeatures=-1&request=GetFeature&typename=gic_collecte.gicsiloverre&SRSNAME=urn:ogc:def:crs:EPSG::4171"

    request(trash_dataset_download_link, { json: true }, (err, res, body) => {
        if (err) { return console.log(err); }
        else {
            first_trash = true;
            body.features.forEach(element => {
                var trash_id = new mongoose.mongo.ObjectId();
                var props = element.properties;
                var coords = element.geometry.coordinates;
                poubelle.create({
                    _id: trash_id,
                    id_grandlyon: props.identifiant,
                    code_insee: props.code_insee,
                    code_postal: props.code_postal,
                    commune: props.commune,
                    gestionnaire: props.gestionnaire,
                    observation: props.observation,
                    numero_voie: props.numerodansvoie,
                    voie: props.voie,
                    adresse: props.numerodansvoie + " " + props.voie + "\n" + props.code_postal + " " + props.commune,
                    longitude: coords[0],
                    lattitude: coords[1],
                    remplissage: Math.random() > 0.1 ? 0. : 1.
                });

                if (Math.random() < 0.10)
                    create_dummy_sensor(trash_id);
                if (first_trash) {
                    first_trash = false;
                    // Add default sensor
                    capteur.create({
                        _id: real_capteur_id,
                        tokenCapteur: "348534593696437587487920546496919",
                        idPoubelle: trash_id
                    });
                }
            });
        }
    });

    // Populate decheteries
    // trash_dataset_download_link = "https://download.data.grandlyon.com/wfs/grandlyon?SERVICE=WFS&VERSION=2.0.0&outputformat=GEOJSON&maxfeatures=30&request=GetFeature&typename=gip_proprete.gipdecheterie&SRSNAME=urn:ogc:def:crs:EPSG::4171"
    // request(trash_dataset_download_link, { json: true }, (err, res, body) => {
    //     if (err) { return console.log(err); }
    //     else {
    //         body.features.forEach(element => {
    //             var props = element.properties;
    //             var coords = element.geometry.coordinates;
    //             decheterie.create({
    //                 _id: new mongoose.mongo.ObjectId(),
    //                 id_grandlyon: props.identifiant,
    //                 code_insee: props.code_insee,
    //                 code_postal: props.code_postal,
    //                 telephone: props.telephone,
    //                 commune: props.commune,
    //                 gestionnaire: props.gestionnaire,
    //                 numero_voie: props.numerodansvoie,
    //                 voie: props.voie,
    //                 adresse: props.numerodansvoie + " " + props.voie + "\n" + props.code_postal + " " + props.commune,
    //                 longitude: coords[0],
    //                 lattitude: coords[1]
    //             });
    //         });
    //     }
    // });



    // Add default associations
    var assoc_id1 = new mongoose.mongo.ObjectId();
    association.create({
        _id: assoc_id1,
        nom: "Les Restos du Coeur",
        description: "Aujourd'hui on a plus le droit...",
        logoUrl: "https://upload.wikimedia.org/wikipedia/fr/thumb/a/ad/Restos_du_coeur_Logo.svg/1024px-Restos_du_coeur_Logo.svg.png",
        rib: 8458853495302923
    });
    var assoc_id2 = new mongoose.mongo.ObjectId();
    association.create({
        _id: assoc_id2,
        nom: "Sidaction",
        description: "Financez la recherche",
        logoUrl: "https://upload.wikimedia.org/wikipedia/fr/thumb/0/0b/Logo_Sidaction.svg/925px-Logo_Sidaction.svg.png",
        rib: 8458853495302923
    });

    create_dummy_users(assoc_id1, assoc_id2);
}

populate_db();
