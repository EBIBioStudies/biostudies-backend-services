INSERT INTO Section(tableIndex, type, ord) VALUES(-1, 'Project', 0);

INSERT INTO Submission(CTime, MTime, RTime, accNo, relPath, released, version, owner_id, submitter_id, secretKey, status)
VALUES(0, 0, 0, 'EuropePMC', 'EuropePMC', 1, 1, 3, 3, 'secret-key', 'PROCESSED');

UPDATE Section SET submission_id = 1;

UPDATE Submission SET rootSection_id = 1;

INSERT INTO Submission_AccessTag VALUES(1, 2);

INSERT INTO SubmissionAttribute(name, reference, value, submission_id, ord)
VALUES('AccNoTemplate', false, '!{S-EPMC}', 1, 0);
