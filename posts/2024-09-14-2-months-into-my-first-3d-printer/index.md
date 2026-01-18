---
title: "2 months into my first 3D printer"
date: 2024-09-14T19:26:06Z
tags: ["3d printing"]
---
[![Ender 3 V3 SE](printer.thumb.jpg)](printer.jpg)

## Pre-arrival

I ordered the Ender 3 V3 SE while it was on sale at C$229 on 2024-06-29 from Creality directly. It arrived a week later on 2024-07-05.

## Problems start - extruder and build plate

Upon receiving it, I couldn't get anything to come out of the nozzle and this was ultimately a combination of bad instruction manual and improper seating of the ribbon cable connector that attaches to the extruder body. The last pin on it was the extruder gear, and it was not attached properly.

Wondering if the problem was z-level offsets, I tried to level the bed and instead drove the nozzle right through it. I replaced the first bed with a dual-sided PEI and PET bed, one with a fancy carbon fibre pattern on one side.
It took two days for my fiancée to notice the actual problem to its full extent and rectify it.

## Slicer - Cura

We had started using Cura when we received the printer, via nixpkgs. We moved to using the latest version on our Windows desktop to get the required profile changes for our printer. Ultimately, I disliked Cura, but its use did not change for months.
## Problems continue - screen detatchment

A week or so into ownership of the printer, the heat-set inserts that connect the screen attachment to the body of the printer itself came out, freeing the screen from the actual body itself without having a heat-set insert attachment for your soldering iron from which to fix them.

I 3D-printed a [standalone mount](https://www.printables.com/model/575623) that let me relocate the screen away from the printer body itself.

## Despite the problems, still enjoyable!

I had started getting into gridfinity and multiboard during the whole process of becoming accustomed to my printer. I had been watching Zack Freedman for a very long time prior to my purchase. I was mostly using PLA and I hadn't investigated getting bed levelling good enough to actually print PETG onto the bed itself.

The stock firmware seemed to do a terrible job at actually making sure the bed itself was level.

We had purchased high-speed 0.6mm and 0.4mm nozzles to be able to extrude filament faster for certain kinds of print. 0.6mm nozzles were worth it as a change from time to time where resolution is less of a priority.

## Creality are awful at everything and anything!

I bought myself a filament sensor attachment on 2024-07-16. On 2024-07-31, I also purchased an LED strip upgrade kit. I was working myself up to doing the install for them, but I could tell by looking up details about these that the process was going to be arduously fucked.

The instruction manual was very, very wrong. The connector on the motherboard is 4 pins and marked FILAM, but the connector they point out on the instruction manual is a 3 pin one. Equally, the method they provide of routing it through the aluminum extrusion... in doing this, I actually broke the cables of both of the upgrades all in one go, on 2024-08-02.

The LED strips, while they had routing places in the arms for them and the top of the gantry, I discovered that when I used them it would clamp the wires regardless.

The printer itself is okay, but I can't help but think there are better printers and printer manufacturers out there. I would rather Bambu Labs, now.

## Klipper, or why the stock firmware sucks ass and Creality are assholes

I had noticed that my prints were alarmingly slow, and that Creality sold "tablets" which improved print speed. This indicated to me that they effectively artificially limited the power of the printer within their firmware.

On 2024-08-06, we endeavoured to replace that firmware with a [custom version of klipper adjusted for the printer](https://github.com/jpcurti/ender3-v3-se-klipper-with-display). You can see the current version of the configurations we are running [here](https://github.com/gensokyo-zone/infrastructure/tree/main/nixos/klipper). We are building the package [here](https://github.com/gensokyo-zone/infrastructure/tree/main/nixos/klipper). We should probably move to the upstream of that repository instead of my fork.

We were already running Octoprint on a laptop (my prior laptop before upgrading to the Framework, a Thinkpad X260) near the printer. We continued using this for Klipper.

The migration to Klipper with Moonraker and Fluidd went well and we managed to have Motion continue to provide webcam services for the 3D printer.

Once we had switched over to Klipper, it was obvious from the print speedups that it was not artificially limited like the stock firmware.

## Filament Holder Changes

These changes to the filament holder were performed on 2024-08-13.

* [608 bearing filament spool holder](https://www.printables.com/model/792537-ender-3-v3-se-608-bearing-filament-spool-and-sampl/) - The body and pins are printed at 40% infill. The roller and the spacers were printed at 20% infill.
* [90 degree offset spool adapter](https://www.thingiverse.com/thing:6631744) - This was printed at 40% infill, I think?

These significantly reduced the vibrations caused by the printer requesting more filament from the spool, which improved print quality a surprising amount.

## Slicer - Orca Slicer

We switched to Orca Slicer on 2024-08-23. We had to derive a customized version of the Ender 3 V3 SE printer profile to use the Klipper gcodes and macros. Switching away from Cura on Klipper made our prints 10 minutes faster at least, to begin with.

## Second attempt at the Creality-branded upgrades

I spent an evening time taking apart the cables for the printer and replacing the long stretches of cable that had been damaged by attaching the functional parts of the wire up to be usable over dupont connectors. (This can be seen in the above image!)

For anyone who has purchased the Creality official LED strip and filament sensor upgrades, I genuinely strongly recommend not running them at all through the aluminum extrusion and instead running them on the outside of the printer, however ugly this appears to be.

## Moving the system away from the laptop to an SBC

My fiancée had a Pine-A64 (non-LTS) that she was not using, and offered it to run the printer stack. The NixOS configurations for it are [here](https://github.com/gensokyo-zone/infrastructure/blob/main/systems/sakuya/nixos.nix).

The relevant modules/folders can be found [here](https://github.com/gensokyo-zone/infrastructure/tree/main/nixos) within that folder.

I 3D-printed a [case](https://www.printables.com/model/301005-pine-a64-case) before we relocated it.

The printer now lives in my office, like shown in the picture at the top of this post.

## Conclusion

Don't buy things from Creality. Ideally, buy Bambu.

Despite this, I love my 3D printer and I love 3D printing. It has gotten me to learn CAD software like Fusion 360 and learn a lot more about the world in general.

## The future?

In the future, I'm likely to get a G10 bed for the purposes of not having to expend glue for prints while still getting decent bed adhesion. If I had known it was an option ahead of time, I definitely would've bought it initially in the first place.

When finances are less rigid, I intend to upgrade the belts on my printer to using linear rails where possible instead.

A full picture of my intended upgrades can be found [here](https://wiki.gensokyo.zone/3D-Printing/ender3v3se#future-upgrades).
