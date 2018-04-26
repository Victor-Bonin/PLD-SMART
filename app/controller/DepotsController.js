var express = require('express');
var router = express.Router();
var mongoose = require('mongoose');

var depot = require("../models/depot");
var user = require("../models/utilisateur");
var association = require("../models/association");

// middleware to use for all requests
router.use(function(req, res, next) {
    console.log('Router middleware log, request : ', req.url); // do logging
    next(); // make sure we go to the next routes and don't stop here
});

// Créer un dépot
router.post('/demarrerScan', function (req, res) {
  console.log("HELLO IM TRYING TO START A DEPOT");
  user.findById(req.body.idUtilisateur, "idAssoc",function (err, utilisateur) {
    if (err) return res.status(500).send("There was a problem finding your association in db");
      var dateNow = new Date();
      var id = new mongoose.mongo.ObjectId();
      depot.create({
        _id: id,
        date: dateNow,
        idUtilisateur: req.body.idUtilisateur,
        idAssoc: utilisateur.idAssoc,
        idCapteur: req.body.idCapteur
      }, function (err, depot) {
        if (err) return res.status(500).send("There was a problem creating your depot in db : "+ err);
        res.status(200).send(depot);
      });
  });
});

// Terminer un dépot en ajoutant le montant
router.put('/terminerScan/:idDepot', function (req, res) {
  console.log("HELLO IM TRYING TO END A DEPOT");
  depot.update({ _id: req.params.idDepot }, { montant: req.body.montant }, function (err, raw) {
    if (err) return res.status(500).send("There was a problem validating your depot in db : "+ err);
    depot.findById(req.params.idDepot, "date montant idAssoc",function (err, infos) {
      res.status(200).send(infos);
    });
  });
});

// TEST : récupération de tous les dépots
router.get('/', function (req, res) {
  console.log("HELLO IM TRYING TO GET ALL THE DEPOTS")
  depot.find({}, function (err, depot) {
    if (err) return res.status(500).send("There was a problem finding depots in db : "+ err);
    res.status(200).send(depot);
  });
});

// TODO : limiter le nombre de dépots récupérés ?
// TODO : Pas propre
router.get('/historique/:id', function (req, res) {
  var userId = req.params.id;
  console.log("HELLO IM TRYING TO GET THE DEPOTS OF USER "+userId)
  depot.find({ 'idUtilisateur': userId}, "date montant idAssoc", function (err, depots) {
    if (err) return res.status(500).send("There was a problem finding your depots in db : "+ err);
        for(var key in depots) {
          if (depots.hasOwnProperty(key)) {
            association.findById(depots[key]['idAssoc'], "nom", function(err, assocs) {
              depots[key].nomAssoc = assocs.nom;
              delete depots[key].idAssoc;
              // console.log(depots[key].nomAssoc);
              // console.log(depots[key]["nomAssoc"]);
              // console.log(depots[key].nomAssoc);
              console.log(depots[key]);
            });
          }
        }
        res.status(200).send(depots);
      });
  });


module.exports = router;
