var p1 = new Promise((res,rej) => res({p2: new Promise((res,rej) => rej(42))}));

p1.then(
  (p) => {
    p.p2.then(console.log, (e) => {throw e;});
  }).catch((e) => console.log("err", e));
