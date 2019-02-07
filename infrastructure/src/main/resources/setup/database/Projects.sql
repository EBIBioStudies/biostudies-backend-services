INSERT INTO Section(tableIndex, type, ord) VALUES(-1, 'Project', 0);
INSERT INTO Submission(CTime, MTime, RTime, accNo, relPath, released, version, owner_id) VALUES(0, 0, 0, 'EuropePMC', 'EuropePMC', 1, 1, 3);
UPDATE Section SET submission_id = 1;
UPDATE Submission SET rootSection_id = 1;
